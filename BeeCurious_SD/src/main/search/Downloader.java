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
public class Downloader extends UnicastRemoteObject implements DownloaderINTER{

    GatewayINTER gateway;
    String downloader_name;
    QueueInterface urlQueue;


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
     * Metodo principal para execução do downloader.
     *
     */
    public void executar() throws RemoteException {
        try {
            int tentativas=0;
            while (true) {
                String url = gateway.getNextURL();
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
     * Processa uma página da web e extrai informações relevantes.
     *
     * @param url A URL da página a ser processada.
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

    private String detectLanguage(String text) {
        text = text.toLowerCase();

        // Contar ocorrências de palavras típicas de cada idioma
        int pt = countMatches(text, " o ", " a ", " os ", " as ", " de ", " do ", " da ");
        int en = countMatches(text, " the ", " and ", " to ", " of ", " a ", " in ");
        int es = countMatches(text, " el ", " la ", " los ", " las ", " de ", " en ");

        if (pt > en && pt > es) return "pt";
        if (es > en && es > pt) return "es";
        return "en"; // padrão
    }

    private int countMatches(String text, String... words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) count++;
        }
        return count;
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
     * Metodo principal que inicializa o downloader.
     *
     * Este metodo é responsável por inicializar o downloader, registrar o downloader no registro RMI
     * e iniciar a execução da thread de download.
     *
     * @param args Argumentos passados para o metodo principal, com o nome do downloader.
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
