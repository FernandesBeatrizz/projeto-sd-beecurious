package main.search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.util.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Downloader extends UnicastRemoteObject implements DownloaderINTER, Runnable, ClienteINTER {

    private static final int timeout = 5000;
    GatewayINTER gateway;
    private static final Set<String> urlsProcessados= new HashSet<>();
    QueueInterface urlQueue;
    List<Barrels> barrel;


    public Downloader(QueueInterface urlQueue) throws RemoteException, InterruptedException {
        super();
        this.urlQueue = urlQueue;
    }

    public void executar(){
        try {
            String rmiName = "gateway";
            String rmiHost = "localhost";
            int rmiPort = 8183;

            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            gateway = (GatewayINTER) registry.lookup(rmiName);
            //gateway.registerClient((ClienteINTER) this);  //ver isto tbm

            int tentativas=0;
            while (true) {
                //String url = gateway.takeNext();
                String url= urlQueue.getURL();
                System.out.println(url);
                if(url == null){
                    //break;
                    System.out.println("fila vazia");
                    /*tentativas+=1;
                    if (tentativas > urlQueue.getMaxSize()) {  // Melhor usar a variável
                        System.out.println("Fila vazia por muito tempo. Encerrando processo.");
                        break;
                    }
                    Thread.sleep(1000);*/
                    continue;
                }
                tentativas=0;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = transformarUrlAbsoluta("http://" + new URI(url).getHost(), url); // Use uma baseUrl apropriada
                }
                if (!urlsProcessados.contains(url.trim().toLowerCase())) {
                    urlsProcessados.add(url.trim().toLowerCase());
                    processarPagina(url);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void processarPagina(String url) {
        try{
            System.out.println("processar o url: "+  url);
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            extrairLinks(doc);
            extrairpalavras(doc,url);
        }catch(IOException e){
            System.out.println("Erro"+ e.getMessage());
        }
    }*/

    /*public void extrairLinks(Document doc) throws RemoteException {
        Elements anchors = doc.select("a");
        String baseUrl = doc.baseUri(); // A URL original do cliente

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

    }*/



    public void processarPagina(String url){
        Barrels barrel= this.barrel.get(0);
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
                for (String palavra : palavras) {
                    if (palavra.length() > 3) { // Evita palavras curtas
                        index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                        gateway.addToIndex(palavra, url);
                    }
                }
                for (Barrels b:this.barrel){
                    b.addToIndex( palavra, url, titulo, citacao, listaLinks);
                }
            }catch (IOException e){
                System.out.println("Erro"+ e.getMessage());
            }
        }
    }



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

    /*public void extrairpalavras(Document doc, String url) throws RemoteException {
        HashMap<String, HashSet<String>> index = new HashMap<>(); // Índice invertido local
        String[] palavras = doc.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
        for (String palavra : palavras) {
            if (palavra.length() > 3) { // Evita palavras curtas
                index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                gateway.addToIndex(palavra, url);
            }
        }
    }*/

@Override
    public void run() {
        executar();
    }

    public static void main(String[] args) {
        try {
            int numDownloaders=3;
            //URLqueue urlQueue= new URLqueue(100);
            Registry registry = LocateRegistry.getRegistry("localhost", 8183);
            GatewayINTER gateway = (GatewayINTER) registry.lookup("gateway");
            QueueInterface urlQueue = gateway.getUrlQueue();

            Thread[] threads = new Thread[numDownloaders];
            for (int i = 0; i < numDownloaders; i++) {
                Downloader d= new Downloader(urlQueue);
                threads[i]= new Thread(d);
                threads[i].start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void printOnClient() throws RemoteException {
        System.out.println("Print on client");
    }

    public String get_url() throws RemoteException {
        return gateway.get_url();
    }

    public void put_url(String url) throws RemoteException {
        gateway.putNew(url);
    }

    public void save_words(String word, String url) throws java.rmi.RemoteException {
        gateway.addToIndex(word, url);
    }

}