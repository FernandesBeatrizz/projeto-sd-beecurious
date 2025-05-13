package main.java.search;

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
 * <p>O {@code Downloader} conecta-se à gateway, procura URLs da fila e extrai informações como
 * título, citação, links e palavras-chave, as quais são registradas e indexadas.</p>
 */
public class Downloader extends UnicastRemoteObject implements DownloaderINTER{

    GatewayINTER gateway;
    String downloader_name;
    QueueInterface urlQueue;


    /**
     * Construtor da classe Downloader.
     *
     * @param name Nome do downloader.
     * @throws RemoteException Se ocorrer erro de comunicação remota.
     */
    public Downloader(String name) throws RemoteException, InterruptedException {
        super();
        this.downloader_name=name;
    }

    /**
     * Cria e regista uma nova instância de Downloader no RMI.
     *
     * @param downloader_nome Nome do downloader.
     * @return A instância criada do Downloader.
     */
    public static Downloader criarDownloader(String downloader_nome) {
        try {
            Downloader novo = new Downloader(downloader_nome);
            Registry registry = LocateRegistry.getRegistry("localHost", 8183);
            registry.rebind(downloader_nome, novo);

            novo.gateway = (GatewayINTER) registry.lookup("Gateway");
            novo.gateway.registerDownloader(novo);
            novo.urlQueue = novo.gateway.getQueue();
            return novo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Inicia o ciclo de execução, vai procurar e processar URLs continuamente.
     *
     * <p>O método aguarda URLs disponíveis na fila e processa cada um. Encerra até
     * que a fila esteja vazia por muito tempo.</p>
     *
     * @throws RemoteException Se ocorrer erro de comunicação remota.
     */
    public void executar() throws RemoteException {
        try {
            int tentativas=0;
            while (true) {
                String url = gateway.getNextURL();
                System.out.println("URL solicitado: "+url);
                System.out.println(url);
                if(url == null){
                    System.out.println("fila vazia");
                    tentativas+=1;
                    if (tentativas > urlQueue.getMaxSize()) {
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
                processarPagina(url);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Processa uma página web especificada pela URL.
     *
     * <p>Este método extrai informações relevantes da página. </p>
     *
     * @param url URL da página a ser processado.
     * @throws RemoteException Se ocorrer erro de comunicação remota.
     */
    public void processarPagina(String url) throws RemoteException {
        BarrelsINTER barrel = gateway.getBarrel();
        if (!barrel.containsURL(url)) {
            try {
                Document doc = Jsoup.connect(url).get();
                // Extrair título
                String titulo = doc.title();
                System.out.println(titulo);
                // Extrair citação
                String citacao = "";
                Element primeiroparagrafo = doc.select("p").first();
                if (primeiroparagrafo != null) {
                    citacao = primeiroparagrafo.text();
                } else {
                    citacao = doc.select("meta[property=og:description]").attr("content");
                    if (citacao.isEmpty()) {
                        citacao = doc.select("meta[name=description]").attr("content");
                    }
                    if (citacao.isEmpty()) {
                        citacao = "Nenhuma citação encontrada.";
                    }
                }
                System.out.println("Citação: " + citacao);
                // Extrair links
                Elements anchors = doc.select("a");
                String baseUrl = doc.baseUri();
                List<String> listaLinks = new ArrayList<>();
                for (Element anchor : anchors) {
                    String href = anchor.attr("href");
                    if (href.isEmpty() || href.startsWith("#")) {
                        continue;
                    }
                    String absoluteUrl = transformarUrlAbsoluta(baseUrl, href);
                    if (absoluteUrl != null) {
                        gateway.putNew(absoluteUrl);
                        listaLinks.add(absoluteUrl);  // Adiciona à lista de links
                        System.out.println("Link extraído: " + absoluteUrl);
                    }
                }
                // Extrair palavras
                String textoCompleto = doc.text().toLowerCase();
                // Remove pontuação e divide em palavras
                String[] palavras = textoCompleto.replaceAll("[^a-z0-9áéíóúãõâêôç\\s]", " ")
                        .split("\\s+");
                for (String palavra : palavras) {
                    if (!palavra.isEmpty()) {
                        gateway.addToIndex(palavra, url, titulo, citacao, listaLinks);
                    }
                }
                // Detectar idioma
                String language = detectLanguage(titulo + " " + citacao);
                // Extrair palavras únicas da página
                String texto = doc.text().toLowerCase();
                Set<String> palavrasUnicas = new HashSet<>(
                        Arrays.asList(texto.replaceAll("[^a-z0-9áéíóúãõâêôç\\s]", " ")
                                .split("\\s+"))
                );
                // Registrar ocorrências de palavras
                for (String palavra : palavrasUnicas) {
                    if (!palavra.isEmpty() && palavra.length() > 2) { // Ignorar palavras muito curtas
                        barrel.registerWordOccurrence(palavra, url, language);
                    }
                }// Indexar apenas palavras não stop
                Set<String> currentStopWords = barrel.getStopWords(language);
                for (String palavra : palavrasUnicas) {
                    if (!currentStopWords.contains(palavra)) {
                        gateway.addToIndex(palavra, url, titulo, citacao, listaLinks);
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro ao processar página: " + e.getMessage());
            }
        }
    }



    /**
     * Detecta o idioma principal de um texto com base em palavras frequentes.
     *
     * @param text Texto a ser analisado.
     * @return Código do idioma detetado ("pt", "en", ou "es").
     */
    private String detectLanguage(String text) {
        text = text.toLowerCase();
        // Contar ocorrências de palavras típicas de cada idioma
        int pt = countMatches(text, " o ", " a ", " os ", " as ", " de ", " do ", " da ", "em", "entre", "para", "um", "ou", "com", "onde");
        int en = countMatches(text, " the ", " and ", " to ", " of ", " a ", " in ", "for", "one", "on", "with", "that", "this", "where");
        int es = countMatches(text, " el ", " la ", " los ", " las ", " de ", " en ", "uno", "cierto", "donde", "hoy", "pero", "si", "y", "una");

        if (pt > en && pt > es) return "pt";
        if (es > en && es > pt) return "es";
        return "en"; // padrão
    }


    /**
     * Conta quantas palavras de um conjunto aparecem no texto.
     *
     * @param text Texto a ser analisado.
     * @param words Palavras a contar.
     * @return Número de ocorrências das palavras no texto.
     */
    private int countMatches(String text, String... words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) count++;
        }
        return count;
    }

    /**
     * Converte um URL relativa em um URL absoluta, com base num URL base.
     *
     * @param baseUrl URL base.
     * @param href URL relativa.
     * @return URL absoluta.
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
     * Metodo principal que inicializa o downloader.
     *
     * <p>Este método é responsável por criar e registar o downloader no registro RMI
     * e iniciar o seu ciclo de execução.</p>
     *
     * @param args Argumentos passados para o método principal, sendo o primeiro o nome do downloader.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public static void main(String[] args) throws RemoteException{
        try {
            String name = args[0];

            Downloader downloader = criarDownloader(name);
            downloader.executar();

            System.out.println("- - downloader " + name + " parou");
        } catch (Exception e) {
            System.out.println("Erro ao executar downloader: " + e.getMessage());
        }
    }
}
