package main.search;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
/**
 * Classe que implementa o servidor Barrel para armazenamento dos dados.
 *
 * <p>Esta classe é responsável por armazenar parte do índice invertido e fornecer funcionalidades de pesquisa. Opera como um servidor RMI que pode ser replicado
 * para garantir tolerância a falhas.</p>-- NAO SEI SE É NECESSARIO
 */
public class Barrels extends UnicastRemoteObject implements BarrelsINTER {
    private HashMap<String, ArrayList<String[]>> indiceInvertido;
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
            Registry registry = LocateRegistry.getRegistry("localhost", gateway_port);
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

            // Mantém o Barrel ativo
            while(true) {
                System.out.println("Barrel ativo...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

            /*
            //p ver s a FUNCIONALIDADE 3 esta a dar--------------------------
            barrel1.addToIndex("borba", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade em Portugal conhecida pelo seu mármore.", new ArrayList<>());
            barrel1.addToIndex("mármore", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade em Portugal conhecida pelo seu mármore.", new ArrayList<>());
            barrel1.addToIndex("portugal", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade em Portugal conhecida pelo seu mármore.", new ArrayList<>());
            // este for é p ver como ele lidava com mais d 10
            for (int i = 1; i <= 15; i++) {
                String termo = "borba";
                String url = "https://exemplo.com/borba" + i;  // URL única para cada entrada
                String titulo = "Cidade de Borba " + i;
                String citacao = "Borba é uma cidade em Portugal conhecida pelo seu mármore. Entrada " + i;
                barrel1.addToIndex(termo, url, titulo, citacao, new ArrayList<>());
            }

            //estas 2 de baixo era p testar a funcionalidade 3. ela esta a dar mas o output p borba é palavra nao encontrada
            String termos="borba";
            barrel1.top10(termos);*/




            /*
            //Para testar a funcionalidade 5-----------------------
            barrel1.addToIndex("cidade", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade conhecida...", Arrays.asList("https://outroexemplo.com"));
            barrel1.addToIndex("mármore", "https://exemplo.com/borba", "Cidade de Borba", "Borba é conhecida pelo seu mármore...", Arrays.asList("https://outroexemplo.com"));


            barrel1.addToIndex("tecnologia", "http://example1.com", "Página de Tecnologia", "A tecnologia está evoluindo rapidamente e novas inovações estão sendo feitas todos os dias.", Arrays.asList("http://example2.com", "http://example3.com", "http://example4.com"));
            barrel1.addToIndex("tecnologia", "http://example2.com", "Página de Ciência", "A ciência e a tecnologia são fundamentais para o progresso da humanidade.", Arrays.asList("http://example1.com", "http://example3.com"));
            barrel1.addToIndex("tecnologia", "http://example3.com", "Página de Programação", "Programação é uma habilidade essencial, e a tecnologia é seu alicerce.", Arrays.asList("http://example1.com"));
            barrel1.addToIndex("tecnologia", "http://example4.com", "Página de Redes", "Redes de comunicação são fundamentais para a evolução da tecnologia.", Arrays.asList("http://example1.com", "http://example5.com"));
            barrel1.addToIndex("tecnologia", "http://example5.com", "Página de Inovação", "Inovações tecnológicas estão mudando o mundo.", Arrays.asList("http://example4.com"));

            // Testando a funcionalidade de pesquisa
            String termos = "tecnologia";
            barrel1.top10(termos); // Vai exibir resultados relacionados a "tecnologia", com diferentes backlinks

            // Teste com outro termo, como "ciência"
            termos = "ciência";
            barrel1.top10(termos);

    */

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
    public void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws
            RemoteException {
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
     * @param word Termo a ser pesquisado
     * @return Lista de URLs que contêm o termo
     */
    @Override
    public synchronized List<String> searchWord(String word) throws RemoteException {
        String[] words = word.toLowerCase().split(" ");

        if (words.length == 0)
            return new ArrayList<>();

        ArrayList<String> resultadourls = new ArrayList<>();
        //ArrayList<String>> sortedURLS = new ArrayList<>();

        if (indiceInvertido.containsKey(words[0])) {
            for (String[] pagina : indiceInvertido.get(words[0])) {
                resultadourls.add(pagina[0]);
            }
        } else {
            return resultadourls; // Se o primeiro termo não existe no índice, retorna lista vazia
        }

        for (int i = 1; i < words.length; i++) {
            if (indiceInvertido.containsKey(words[i])) {
                resultadourls.retainAll(indiceInvertido.get(words[i])); //addAll ou retainAll
            } else {
                //resultadourls.clear(); //ver bem este else
                //break;
                return new ArrayList<>();
            }
        }
        return resultadourls;
    }


    public void ping() throws RemoteException{
    }


    @Override
    public void updateIndex(HashMap<String, ArrayList<String>> indiceParaPesquisas) throws RemoteException {

    }

    public void reviverBarrel() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("localhost", 8183);
        try {
            registry.unbind(this.name);
            this.gateway.unregisterBarrel(this);
            System.out.println("Barrel " + this.name + " removido do RMI Registry.");
            criarbarrel(name, 8183);
        } catch (Exception e) {
            System.out.println(" erro a reviver barrel");
        }

    }

    @Override
    public void indexarURL(String url) throws RemoteException {

    }


    private int pagina = 1;
    private final Scanner sc = new Scanner(System.in);

    public List<String[]> top10(String termos) throws RemoteException {
        Scanner sc = new Scanner(System.in);
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

        //ordeno os resultados pela quantidade de termos encontrados
        /*resultados.sort(new Comparator<String[]>() {
            public int compare(String[] pag1, String[] pag2) {
                int count1 = contarTermosNaPagina(pag1, palavras);
                int count2 = contarTermosNaPagina(pag2, palavras);

                if (count1 == count2) {
                    int ponteiro1 = obterrelevancia(pag1[0]);
                    int ponteiro2 = obterrelevancia(pag2[0]);
                    return Integer.compare(ponteiro1, ponteiro2);
                }
                return Integer.compare(count2, count1);
            }
        });*/
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

        int resultadospag = 10;
        int totalPaginas = (int) Math.ceil((double) resultados.size() / resultadospag);
        boolean sair = false;


        while (pagina <= totalPaginas && !sair) {
            int inicio = (pagina - 1) * resultadospag; // Página começa a contar de 1
            int fim = Math.min(inicio + resultadospag, resultados.size());

            // Exibe os resultados da página atual
            List<String[]> resultadosDaPagina = resultados.subList(inicio, fim);

            System.out.println("\nResultados da pesquisa para: " + termos + " - Página " + pagina);
            for (String[] resultado : resultadosDaPagina) {
                System.out.println("Título: " + resultado[1]);
                System.out.println("URL: " + resultado[0]);
                System.out.println("Citação: " + resultado[2]);
                System.out.println("---------------------------");
            }

            // Aumenta a página para a próxima chamada
            //pagina++;

            if (pagina <= totalPaginas) {

                System.out.println("Pressione Enter para ver a próxima página ou digite 'sair' para voltar ao menu...");
                String entrada = sc.nextLine();
                if (entrada.equalsIgnoreCase("sair")) {
                    sair = true;
                } else {
                    pagina++;
                }
            } else {
                System.out.println("Fim dos resultados. Pressione Enter para voltar ao menu.");
                sc.nextLine();
                sair = true;
            }
        }

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