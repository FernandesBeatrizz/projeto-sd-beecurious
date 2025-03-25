package main.search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Classe responsável por baixar e processar páginas da web.
 *
 * <p>O Downloader obtém URLs da fila, extrai informações e links, e indexa palavras para referência futura.</p>
 */
public class Downloader extends UnicastRemoteObject implements DownloaderINTER, Runnable {

    GatewayINTER gateway;
    String downloader_name;
    private static final Set<String> urlsProcessados= new HashSet<>();
    QueueInterface urlQueue;
    private static final String nomesficheiroparaguardar = "informacoescompletas.obj";


    /**
     * Construtor da classe Downloader.
     *
     * @param name Nome do downloader.
     */
    public Downloader(String name) throws RemoteException, InterruptedException {
        super();
        this.downloader_name=name;
    }

    /**
     * Cria e registra um novo downloader no RMI.
     *
     * @param downloader_nome Nome do downloader.
     */
    public static void criarDownloader(String downloader_nome) {
        try {
            Downloader novo = new Downloader(downloader_nome);
            Registry registry = LocateRegistry.getRegistry("localhost", 8183);
            registry.rebind(downloader_nome, novo);

            novo.gateway = (GatewayINTER) registry.lookup("Gateway");
            novo.gateway.registerDownloader(novo);
            novo.urlQueue=novo.gateway.getQueue();
        } catch (Exception e) {
            e.printStackTrace();
        }
}


    /**
     * Metodo principal para execução do downloader.
     *
     */
    public void executar() throws RemoteException {
        try {
            int tentativas=0;
            while (true) {
                //String url = gateway.takeNext();
                String url= urlQueue.getURL();
                System.out.println(url);
                if(url == null){
                    //break;
                    System.out.println("fila vazia");
                    tentativas+=1;
                    if (tentativas > urlQueue.getMaxSize()) {  // Melhor usar a variável
                        System.out.println("Fila vazia por muito tempo. Encerrando processo.");
                        break;
                    }
                    Thread.sleep(1000);
                    continue;
                }
                tentativas=0;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = transformarUrlAbsoluta("http://" + new URI(url).getHost(), url);
                }
                if (!urlsProcessados.contains(url.trim().toLowerCase())) {
                    urlsProcessados.add(url.trim().toLowerCase());
                    processarPagina(url);
                    urlQueue.markURLAsProcessed(url);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void processarPagina(String url) throws RemoteException {
        BarrelsINTER barrel= gateway.getBarrel();
        if (!barrel.containsURL(url)){
            try{
                Document doc= Jsoup.connect(url).get();

                //titulo
                String titulo = doc.title();
                System.out.println(titulo);


                //citação
                String citacao= "";
                Element primeiroparagrafo= doc.select("p").first();
                if (primeiroparagrafo != null) {
                    citacao = primeiroparagrafo.text();
                }else{
                    citacao = doc.select("meta[property=og:description]").attr("content");
                    if (citacao.isEmpty()) {
                        citacao = doc.select("meta[name=description]").attr("content");
                    }
                    if (citacao.isEmpty()) {
                        citacao = "Nenhuma citação encontrada.";
                    }
                }
                System.out.println("Citação: " + citacao);


                //Extrair link
                Elements anchors = doc.select("a");
                String baseUrl = doc.baseUri(); // A URL original do cliente
                List<String> listaLinks = new ArrayList<>();

                if (baseUrl.isEmpty()) {
                    System.err.println("Erro: baseUri() não foi definido corretamente!");
                    return;
                }
                for (Element anchor : anchors) {
                    String href = anchor.attr("href");
                    if (href.isEmpty() || href.startsWith("#")) {
                        continue;
                    }
                    String absoluteUrl=transformarUrlAbsoluta(baseUrl, href);
                    if (absoluteUrl != null) {
                        gateway.putNew(absoluteUrl);
                        System.out.println("Link extraído: " + absoluteUrl);
                    }
                }


                //extrair palavras
                HashMap<String, HashSet<String>> index = new HashMap<>(); // Índice invertido local
                String[] palavras = doc.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
                Set<String> palavrasExtraidas = new HashSet<>();
                for (String palavra : palavras) {
                    if (palavra.length() > 3) { // Evita palavras curtas
                        index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                        gateway.addToIndex(palavra, url);

                        palavrasExtraidas.add(palavra);
                    }
                }

                for (String palavra : palavrasExtraidas) {
                    barrel.addToIndex(palavra, url, titulo, citacao, listaLinks);
                }
            }catch (IOException e){
                System.out.println("Erro"+ e.getMessage());
            }
        }
    }*/

    /**
     * Processa uma página da web e extrai informações relevantes.
     *
     * @param url A URL da página a ser processada.
     */
    public void processarPagina(String url) throws RemoteException {
        BarrelsINTER barrel= gateway.getBarrel();
        if (!barrel.containsURL(url)){
            try{
                Document doc= Jsoup.connect(url).get();

                //titulo
                String titulo = doc.title();
                System.out.println(titulo);


                //citação
                String citacao= "";
                Element primeiroparagrafo= doc.select("p").first();
                if (primeiroparagrafo != null) {
                    citacao = primeiroparagrafo.text();
                }else{
                    citacao = doc.select("meta[property=og:description]").attr("content");
                    if (citacao.isEmpty()) {
                        citacao = doc.select("meta[name=description]").attr("content");
                    }
                    if (citacao.isEmpty()) {
                        citacao = "Nenhuma citação encontrada.";
                    }
                }
                System.out.println("Citação: " + citacao);


                //Extrair link
                Elements anchors = doc.select("a");
                String baseUrl = doc.baseUri(); // A URL original do cliente
                List<String> listaLinks = new ArrayList<>();

                if (baseUrl.isEmpty()) {
                    System.err.println("Erro: baseUri() não foi definido corretamente!");
                    return;
                }
                for (Element anchor : anchors) {
                    String href = anchor.attr("href");
                    if (href.isEmpty() || href.startsWith("#")) {
                        continue;
                    }
                    String absoluteUrl=transformarUrlAbsoluta(baseUrl, href);
                    if (absoluteUrl != null) {
                        gateway.putNew(absoluteUrl);
                        System.out.println("Link extraído: " + absoluteUrl);
                    }
                }


                //extrair palavras
                HashMap<String, HashSet<String>> index = new HashMap<>(); // Índice invertido local
                String[] palavras = doc.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
                Set<String> palavrasExtraidas = new HashSet<>();
                for (String palavra : palavras) {
                    index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                    gateway.addToIndex(palavra, url);
                    palavrasExtraidas.add(palavra);
                }

                for(String palavra : palavrasExtraidas){
                    salvarinformacoesnecessarias(palavra, url, titulo, citacao, listaLinks);
                }

                for (String palavra : palavrasExtraidas) {
                    barrel.addToIndex(palavra, url, titulo, citacao, listaLinks);
                }
            }catch (IOException e){
                System.out.println("Erro"+ e.getMessage());
            }
        }
    }


    /**
     * Converte URLs relativas em URLs absolutas.
     *
     * @param baseUrl URL base.
     * @param href URL relativa.
     * @return URL absoluta ou null em caso de erro.
     */
    private String transformarUrlAbsoluta(String baseUrl, String href){
        try {
            URI baseUri = new URI(baseUrl);

            if (href.startsWith("http://") || href.startsWith("https://")) { //se ja for normal ele retorna so
                return href;
            } else if (href.startsWith("/")) { //se começar por isto ele vai construir
                return baseUri.getScheme() + "://" + baseUri.getHost() + href;
            } else {
                String caminhoAtual = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);
                return caminhoAtual + href;
            }
        } catch (URISyntaxException e) {
            System.err.println("Erro ao processar URL: " + href);
            return null;
        }
    }


    /**
     * Salva as informações necessárias em um arquivo.
     *
     * Este metodo tenta salvar um conjunto de dados que inclui uma palavra, uma URL, o título da página,
     * uma citação extraída da página e uma lista de links encontrados. As informações são armazenadas em um
     * arquivo binário no formato de objetos. Caso o arquivo já exista, ele tenta carregar os dados antigos
     * para verificar se a URL já foi processada. Se a URL já tiver sido processada, o metodo simplesmente
     * retorna sem salvar novamente.
     *
     * @param palavra A palavra extraída do conteúdo da página que será usada para indexação.
     * @param url A URL da página processada.
     * @param titulo O título da página processada.
     * @param citacao A citação extraída do primeiro parágrafo ou metadados da página.
     * @param listaLinks A lista de links extraídos da página.
     */
    public void salvarinformacoesnecessarias(String palavra, String url, String titulo, String citacao, List<String>listaLinks){
        try{
            //ObjectOutputStream out;
            File file= new File(nomesficheiroparaguardar);

            List<HashMap<String, Object>> dadosExistentes = new ArrayList<>();

            // Verificar se o arquivo já existe e carregar dados antigos
            if (file.exists() && file.length() > 0) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                    while (true) {
                        try {
                            HashMap<String, Object> dados = (HashMap<String, Object>) in.readObject();
                            dadosExistentes.add(dados);
                        } catch (EOFException e) {
                            break; // Fim do arquivo
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Erro ao ler dados existentes: " + e.getMessage());
                }
            }

            // Verificar se a URL já foi processada
            for (HashMap<String, Object> dados : dadosExistentes) {
                if (dados.get("url").equals(url)) {
                    System.out.println("Informação já processada");
                    return;
                }
            }

            HashMap<String, Object> dados = new HashMap<>();
            dados.put("palavra", palavra);
            dados.put("url", url);
            dados.put("titulo", titulo);
            dados.put("citacao", citacao);
            dados.put("links", listaLinks);

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file, true)) {
                @Override
                protected void writeStreamHeader() throws IOException {
                    if (file.length() == 0) {
                        super.writeStreamHeader(); // Escreve cabeçalho apenas se o arquivo estiver vazio
                    }
                }
            }) {
                out.writeObject(dados);
                System.out.println("As informações foram salvas com sucesso!");
            }

        } catch (IOException e) {
            System.err.println("Erro ao salvar as informações: " + e.getMessage());
        }
    }


    /**
     * Metodo executado pela thread que faz o download e processa as URLs.
     *
     * Este metodo é a execução principal da thread. Ele é responsável por pegar a próxima URL da fila,
     * verificar se a URL é válida, processá-la, extrair informações dela e, em seguida, marcar a URL como
     * processada. Ele continua em execução até que a fila de URLs esteja vazia por um longo tempo.
     *
     */
@Override
    public void run() {
    try {
        executar();
    } catch (RemoteException e) {
        throw new RuntimeException(e);
    }
}

    /**
     * Obtém a URL da fila de URLs do gateway.
     *
     * Este metodo faz uma chamada ao gateway para obter a URL próxima que precisa ser processada.
     *
     * @return A URL a ser processada.
     */
    public String get_url() throws RemoteException {
        return gateway.get_url();
    }


    /**
     * Coloca uma nova URL na fila do gateway.
     *
     * Este metodo faz uma chamada ao gateway para adicionar uma nova URL à fila de URLs que precisam
     * ser processadas.
     *
     * @param url A URL a ser adicionada à fila.
     */
    public void put_url(String url) throws RemoteException {
        gateway.putNew(url);
    }

    /**
     * Salva uma palavra no índice do gateway.
     *
     * Este metodo envia uma palavra extraída de uma página para ser salva no índice do gateway.
     *
     * @param word A palavra a ser indexada.
     * @param url A URL associada à palavra.
     */
    public void save_words(String word, String url) throws java.rmi.RemoteException {
        gateway.addToIndex(word, url);
    }

    /**
     * Metodo principal que inicializa o downloader.
     *
     * Este metodo é responsável por inicializar o downloader, registrar o downloader no registro RMI
     * e iniciar a execução da thread de download.
     *
     * @param args Argumentos passados para o metodo principal, com o nome do downloader.
     */
    public static void main(String[] args) throws RemoteException{
        String name = args[0];

        criarDownloader(name);

        System.out.println("- - downloader " + name+ " check");

    }

}
