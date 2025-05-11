package main.java.search;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Classe que implementa o servidor Barrel para armazenamento dos dados.
 *
 * <p>Esta classe é responsável por armazenar parte do índice invertido e fornecer funcionalidades de pesquisa. Opera como um servidor RMI que pode ser replicado
 * para garantir tolerância a falhas.</p>-- NAO SEI SE É NECESSARIO
 */
public class Barrels extends UnicastRemoteObject implements BarrelsINTER {
    private HashMap<String, ArrayList<String []>> indiceInvertido;
    private GatewayINTER gateway;
    private static String name;
    private HashMap<String, HashSet<String>> ponteiros;
    private static String ficheiroURLbarrels;
    private static final Object fileLock = new Object();  // Lock estático partilhado por todos os barrels
    private final Map<String, Map<String, Set<String>>> wordPageOccurrences = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> totalPagesPerLanguage = new ConcurrentHashMap<>();
    private Map<String, Set<String>> stopWords = new ConcurrentHashMap<>();
    private static final double STOP_WORD_PERCENTAGE = 0.10;
    private static final int UPDATE_THRESHOLD = 1000; // Atualizar a cada 1000 páginas
    private final String ficheiroStopWords;
    /**
     * Construtor da classe Barrels.
     *
     * @param name Nome do barrel
     */
    public Barrels(String name) throws RemoteException {
        super();
        this.ficheiroStopWords = "stopWords" + name + ".obj";
        this.indiceInvertido = new HashMap<>();
        this.name = name;
        this.ponteiros = new HashMap<>();
        this.ficheiroURLbarrels = name + ".obj";
        carregarIndice();
        System.out.println("[DEBUG] Índice após carregar: " + indiceInvertido);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Executando shutdown seguro...");
            if (!new File(ficheiroURLbarrels).exists()) {
                salvar(); // Garante salvamento final
            }
        }));

    }

    /**
     * Metodo para criar um novo barrel e registá-lo no gateway.
     *
     * @param barrel_nome Nome do barrel
     * @param gateway_port Porta do gateway
     */
    public static Barrels criarbarrel(String barrel_nome, int gateway_port) {
        try {
            Barrels novo = new Barrels(barrel_nome);
            Registry registry = LocateRegistry.getRegistry("localHost", gateway_port);
            registry.rebind(barrel_nome, novo);

            novo.gateway = (GatewayINTER) registry.lookup("Gateway");
            novo.gateway.registerBarrel(novo);
            return novo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Metodo principal para iniciar um barrel.
     *
     * @param args Argumentos de entrada: nome do barrel e porta do gateway
     */
    public static void main(String[] args) {
        try {
            String name = args[0];
            int gateway_port = Integer.parseInt(args[1]);

            Barrels barrel = criarbarrel(name, gateway_port);
            System.out.println("- - barrel " + name + " check");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega o índice invertido a partir do ficheiro, se existir.
     */
    @SuppressWarnings("unchecked")
    private void carregarIndice() {
        File file = new File(ficheiroURLbarrels);
        System.out.println("[DEBUG] Caminho do ficheiro: " + file.getAbsolutePath());
        System.out.println("[DEBUG] Tamanho do ficheiro: " + file.length() + " bytes");

        if (!file.exists() || file.length() == 0) {
            System.out.println("Ficheiro não existe ou está vazio. A criar novo índice.");
            return;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = input.readObject();
            System.out.println("[DEBUG] Objeto desserializado: " + obj.getClass().getName());

            if (obj instanceof HashMap) {
                indiceInvertido = (HashMap<String, ArrayList<String[]>>) obj;
                System.out.println("[DEBUG] Índice carregado com " + indiceInvertido.size() + " termos.");
                reconstruirPonteiros();
            } else {
                System.err.println("Erro: Formato inválido do ficheiro.");
            }
        } catch (EOFException e) {
            System.err.println("Erro: Ficheiro corrompido (EOF inesperado). Criando novo índice.");
            file.delete(); // Remove o ficheiro inválido
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar o índice: " + e.getMessage());
            e.printStackTrace();
        }

        // === Carrega stopWords ===
        File swFile = new File(ficheiroStopWords);
        if (!swFile.exists() || swFile.length() == 0) {
            System.out.println("Ficheiro de stopWords não existe ou está vazio. Iniciando vazio.");
        } else {
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(swFile))) {
                Object obj = input.readObject();
                System.out.println("[DEBUG] Objeto desserializado (stopWords): " + obj.getClass().getName());

                if (obj instanceof Map) {
                    stopWords.clear();
                    stopWords.putAll((Map<String, Set<String>>) obj);
                    System.out.println("[DEBUG] StopWords carregado com " + stopWords.size() + " entradas.");
                } else {
                    System.err.println("Erro: Formato inválido do ficheiro de stopWords.");
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao carregar as stopWords: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Reconstrói o mapa de ponteiros a partir do índice invertido carregado.
     */
    private void reconstruirPonteiros() {
        ponteiros.clear();
        for (ArrayList<String[]> paginas : indiceInvertido.values()) {
            for (String[] pagina : paginas) {
                if (pagina.length >= 4) { // Verifica se tem links
                    String[] links = pagina[3].split(",");
                    for (String link : links) {
                        if (!link.isEmpty()) {
                            ponteiros.putIfAbsent(link, new HashSet<>());
                            ponteiros.get(link).add(pagina[0]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adiciona um termo ao índice invertido.
     *
     * @param word Termo a ser indexado
     * @param url URL associada ao termo
     * @param titulo Título da página
     * @param citacao Citação de texto
     * @param links Lista de links relacionados
     */
    @Override
    public void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException {
        // Obtém o Barrel Principal (O primeiro que processa a URL)
        BarrelsINTER barrelPrincipal = gateway.getBarrel();  // O barrel principal vai indexar

        if (barrelPrincipal != null) {
            String[] pagina = {url, titulo, citacao, String.join(",", links)};
            synchronized (indiceInvertido) {
                if (!indiceInvertido.containsKey(word)) {
                    indiceInvertido.put(word, new ArrayList<>());
                }

                // Verifica se a URL já está indexada para a palavra
                boolean urlExiste = false;
                for (String[] pagina_indice : indiceInvertido.get(word)) {
                    if (pagina_indice[0].equals(url)) {
                        urlExiste = true;
                        break;
                    }
                }

                // Se não existe, adiciona a URL ao índice
                if (!urlExiste) {
                    indiceInvertido.get(word).add(pagina);
                }

                // Log de indexação no barrel principal
                System.out.println("[Barrel] Palavra indexada: " + word + " (URL: " + url + ")");
                salvar();
            }

            // Agora propaga para os outros barrels
            try {
                List<BarrelsINTER> outrosBarrels = gateway.getAllBarrels();
                for (BarrelsINTER barrel : outrosBarrels) {
                    if (!barrel.getName().equals(this.getName())) {
                        // Envia a palavra para os outros barrels
                        barrel.mandarIndex(word, url, titulo, citacao, links);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao propagar a indexação para os outros barrels: " + e.getMessage());
            }
        } else {
            System.err.println("Nenhum barrel principal disponível para indexação!");
        }
    }


    public void mandarIndex(String word, String url, String titulo, String citacao, List<String> links) {
        String[] pagina = {url, titulo, citacao, String.join(",", links)};
        synchronized (indiceInvertido) {
            if (!indiceInvertido.containsKey(word)) {
                indiceInvertido.put(word, new ArrayList<>());
            }

            // Verifica se a URL já foi indexada para a palavra
            for (String[] pagina_indice : indiceInvertido.get(word)) {
                if (pagina_indice[0].equals(url)) {
                    return;  // Não indexa novamente se a URL já estiver presente
                }
            }
            indiceInvertido.get(word).add(pagina);
            salvar();
            System.out.println("[Barrel] Palavra indexada: " + word + " (URL: " + url + ")");
        }
    }


    /**
     * Obtém a lista de páginas que apontam para um determinado URL.
     *
     * @param url O URL de interesse.
     * @return Lista de URLs que apontam para o URL fornecido.
     */
    public List<String> obterpaginaurlponteiros(String url) {
        //primeiro verificamos se a url existe
        if (ponteiros.containsKey(url)) {
            return new ArrayList<>(ponteiros.get(url));
        } else {
            System.out.println("Nenhuma pagina encontrada para este url");
            return new ArrayList<>();
        }
    }

    public void ping() throws RemoteException {
        getName();
    }


    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

    // ---------------PESQUISA---------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------------

    public List<String[]> top10(String termos) throws RemoteException {
        String[] palavras = termos.toLowerCase().split(" ");
        List<String[]> resultados = new ArrayList<>();

        //vou ver s os termos existem no indiceinvertido
        for (String palavra : palavras) {
            if (!indiceInvertido.containsKey(palavra)) {
                System.out.println("palavra não encontrada");
                return resultados; //s nao encontrar nd retorna lista vazia
            }
        }
        resultados.addAll(indiceInvertido.get(palavras[0])); //todas as paginas q contêm o primeiro termo

        // Filtra os resultados para páginas que contêm todos os termos
        for (int i = 1; i < palavras.length; i++) {
            List<String[]> tempResultados = new ArrayList<>();
            Set<String> urlsExistentes = new HashSet<>();

            // Armazena as URLs atuais
            for (String[] pagina : resultados) {
                urlsExistentes.add(pagina[0]);
            }

            // Adiciona páginas que contenham o termo atual e estejam nas URLs anteriores
            for (String[] pagina : indiceInvertido.get(palavras[i])) {
                if (urlsExistentes.contains(pagina[0])) {
                    tempResultados.add(pagina);
                }
            }
            resultados = tempResultados;
        }

        resultados.sort(new Comparator<String[]>() {
            public int compare(String[] pag1, String[] pag2) {
                int ponteiro1 = obterrelevancia(pag1[0]);
                int ponteiro2 = obterrelevancia(pag2[0]);

                if (ponteiro1 == ponteiro2) {
                    int count1 = contarTermosNaPagina(pag1, palavras);
                    int count2 = contarTermosNaPagina(pag2, palavras);
                    return Integer.compare(count1, count2);  //oq tem mais termos primeiro
                }
                return Integer.compare(ponteiro2, ponteiro1); //oq tem mais links primeiro
            }
        });
        return resultados;
    }

    private int contarTermosNaPagina(String[] pagina, String[] termos) {
        int count = 0;
        String conteudo = pagina[1] + " " + pagina[2]; // Título + citação

        for (String termo : termos) {
            if (conteudo.toLowerCase().contains(termo)) {
                count++;
            }
        }

        return count;
    }

    public int obterrelevancia(String url) {
        //basicamente vamos contar os ponteiros
        if (ponteiros.containsKey(url)) {
            return ponteiros.get(url).size();
        }
        return 0;
    }

    /**
     * Salva o índice invertido em um arquivo.
     */
    private void salvar() {
        synchronized (fileLock) {  // Bloqueio a nível de classe
            File tempFile = new File(ficheiroURLbarrels + ".tmp");
            try {
                // Passo 1: Escreve em arquivo temporário
                try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                    output.writeObject(indiceInvertido);
                }

                // Passo 2: Substitui o arquivo principal atomicamente
                Files.move(
                        tempFile.toPath(),
                        new File(ficheiroURLbarrels).toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                System.err.println("Erro ao salvar: " + e.getMessage());
            } finally {
                if (tempFile.exists()) tempFile.delete();  // Limpeza
            }
        }

        File tempSWFile = new File(ficheiroStopWords + ".tmp");
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(tempSWFile))) {
                output.writeObject(stopWords);
            }
            Files.move(
                    tempSWFile.toPath(),
                    new File(ficheiroStopWords).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            System.err.println("Erro ao salvar stopWords: " + e.getMessage());
        } finally {
            if (tempSWFile.exists()) tempSWFile.delete();
        }
    }

    /**
     * Verifica se um URL está contido no índice.
     *
     * @param url URL a ser verificada
     * @return true se o URL está contido, false caso contrário
     */
    public boolean containsURL(String url) {
        for (ArrayList<String[]> paginas : indiceInvertido.values()) {
            for (String[] pagina : paginas) {
                if (pagina[0].equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ----- STOP WORDS ---------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------

    @Override
    public synchronized void registerWordOccurrence(String word, String url, String language) throws RemoteException {
        wordPageOccurrences.putIfAbsent(language, new ConcurrentHashMap<>());
        totalPagesPerLanguage.putIfAbsent(language, new AtomicInteger(0));

        Map<String, Set<String>> languageWords = wordPageOccurrences.get(language);
        languageWords.putIfAbsent(word, ConcurrentHashMap.newKeySet());

        if (languageWords.get(word).add(url)) {
            totalPagesPerLanguage.get(language).incrementAndGet();

            if (totalPagesPerLanguage.get(language).get() % UPDATE_THRESHOLD == 0) {
                recalculateStopWords(language);
            }
        }
    }

    private synchronized void recalculateStopWords(String language) {
        Map<String, Set<String>> languageWords = wordPageOccurrences.get(language);
        if (languageWords == null || languageWords.isEmpty()) return;

        // 1. Contar em quantas páginas cada palavra aparece
        List<Map.Entry<String, Integer>> wordCounts = languageWords.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().size()))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 2. Selecionar as 10% mais comuns
        int stopWordsCount = (int) (wordCounts.size() * STOP_WORD_PERCENTAGE);
        Set<String> newStopWords = wordCounts.stream()
                .limit(stopWordsCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // 3. Atualizar lista de stop words
        stopWords.put(language, newStopWords);
        System.out.printf("[Barrel] Stop words atualizadas para %s: %d palavras (de %d)%n",
                language, stopWordsCount, wordCounts.size());

        // 4. Sincronizar com outros barrels
        syncStopWordsWithOtherBarrels(language, newStopWords);
    }

    @Override
    public Set<String> getStopWords(String language) throws RemoteException {
        return stopWords.getOrDefault(language, Collections.emptySet());
    }

    private void syncStopWordsWithOtherBarrels(String language, Set<String> newStopWords) {
        try {
            List<BarrelsINTER> allBarrels = gateway.getAllBarrels();
            for (BarrelsINTER barrel : allBarrels) {
                if (!barrel.equals(this)) {
                    barrel.updateStopWords(language, newStopWords);
                }
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao sincronizar stop words: " + e.getMessage());
        }
    }

    @Override
    public void updateStopWords(String language, Set<String> stopWords) throws RemoteException {
        this.stopWords.put(language, new HashSet<>(stopWords));
    }

    public Map<String, Set<String>> getAllStopWords() throws RemoteException{
        return stopWords;
    }

    @Override
    public void carregarDados(Map<String, ArrayList<String[]>> indice, Map<String, Set<String>> stopWords) throws RemoteException {
        synchronized (indiceInvertido) {
            this.indiceInvertido = new HashMap<>(indice);
            this.stopWords = new ConcurrentHashMap<>(stopWords);
            reconstruirPonteiros(); // Reconstruir ponteiros após carregar o índice
            salvar(); // Persistir no disco
            System.out.println("Dados sincronizados com sucesso.");
        }
    }

    @Override
    public Map<String, ArrayList<String[]>> getIndiceInvertido() throws RemoteException {
        return new HashMap<>(indiceInvertido);
    }

}