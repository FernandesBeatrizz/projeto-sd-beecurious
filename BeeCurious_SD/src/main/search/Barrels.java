package main.search;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
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

    /**
     * Construtor da classe Barrels.
     *
     * @param name Nome do barrel
     */
    public Barrels(String name) throws RemoteException {
        super();
        this.indiceInvertido = new HashMap<>();
        this.name = name;
        this.ponteiros = new HashMap<>();
        this.ficheiroURLbarrels = name + ".obj";

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
            novo.gateway.syncBarrels();
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
            while (true) {
                System.out.println("indice invertido: " + barrel.indiceInvertido);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        indiceInvertido.putIfAbsent(word, new ArrayList<>());


        boolean urlExiste = false;
        for (String[] pagina : indiceInvertido.get(word)) {
            if (pagina[0].equals(url)) {
                urlExiste = true;
                break;
            }
        }

        if (!urlExiste) {
            // Armazena a URL, título, citação e links como um array
            indiceInvertido.get(word).add(new String[]{url, titulo, citacao, String.join(",", links)});

        }
        //p ver quem aponta
        for (String link : links) {
            ponteiros.putIfAbsent(link, new HashSet<>());
            ponteiros.get(link).add(url); // A URL atual aponta para a página 'link'
        }
        salvar();
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


    /**
     * Pesquisa um termo no índice invertido.
     *
     * @param words Termo a ser pesquisado
     * @return Lista de URLs que contêm o termo
     */
    @Override
    public synchronized List<String[]> searchWord(String words) throws RemoteException {
        String[] terms = words.toLowerCase().split("\\s+");
        List<String[]> results = new ArrayList<>();

        if (terms.length == 0) {
            return results;
        }

        // Para o primeiro termo, adiciona todas as páginas correspondentes
        if (indiceInvertido.containsKey(terms[0])) {
            results.addAll(indiceInvertido.get(terms[0]));
        }

        // Para os termos seguintes, filtra mantendo apenas páginas que contêm todos os termos
        for (int i = 1; i < terms.length && !results.isEmpty(); i++) {
            String term = terms[i];
            if (!indiceInvertido.containsKey(term)) {
                results.clear();
                break;
            }

            List<String[]> termPages = indiceInvertido.get(term);
            results = results.stream()
                    .filter(page -> termPages.stream()
                            .anyMatch(termPage -> termPage[0].equals(page[0])))
                    .collect(Collectors.toList());
        }

        return results;
    }

    public void ping() throws RemoteException{
    }

    @Override
    public void updateIndex(HashMap<String, ArrayList<String[]>> newIndex) throws RemoteException{
        if (newIndex == null) return;

        for (String word : newIndex.keySet()) {
            ArrayList<String[]> page = newIndex.get(word);
            if (!indiceInvertido.containsKey(word)) {
                indiceInvertido.put(word, page);
            }
        }
        salvar();
    }

    public void reviverBarrel() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("localHost", 8183);
        try {
            this.gateway.unregisterBarrel(this);
            registry.unbind(this.name);
            System.out.println("Barrel " + this.name + " removido do RMI Registry.");
            criarbarrel(name, 8183);
        } catch (Exception e) {
            System.out.println(" erro a reviver barrel");
        }

    }

    private int pagina = 1;

    public List<String[]> top10(String termos) throws RemoteException {
        String[] palavras = termos.toLowerCase().split(" ");

        for (String termo : palavras) {
            if (!indiceInvertido.containsKey(termo)) {
                System.out.println(" Palavra '" + termo + "' não encontrada");
                continue;
            }

            List<String[]> paginas = indiceInvertido.get(termo);

            for (String[] pagina : paginas) {
                System.out.println("   Título: " + pagina[1]);
                System.out.println("   URL: " + pagina[0]);
                System.out.println("   ---");
            }
        }

        System.out.println("\nPressione Enter para voltar...");
        new Scanner(System.in).nextLine();

        return indiceInvertido.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
        synchronized (this) {
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(ficheiroURLbarrels))) {
                output.writeObject(indiceInvertido);
                System.out.println("Indice salvo com sucesso.");
            } catch (IOException e) {
                System.err.println("Erro ao salvar o índice: " + e.getMessage());
            }
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
}