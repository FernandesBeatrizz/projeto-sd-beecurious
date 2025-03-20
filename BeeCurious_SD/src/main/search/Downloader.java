package main.search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Downloader extends UnicastRemoteObject implements DownloaderINTER, Runnable, ClienteINTER {

    private static final int timeout = 5000;
    GatewayINTER gateway;
    private static final Set<String> urlsProcessados= new HashSet<>();

    protected Downloader() throws RemoteException {
    }

    public void executar(){
        try {
            String rmiName = "Gateway";
            String rmiHost = "localHost";
            int rmiPort = 8183;

            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            gateway = (GatewayINTER) registry.lookup(rmiName);
            //gateway.registerClient((ClienteINTER) this);  //ver isto tbm

            while (true) {
                String url = gateway.takeNext();
                System.out.println(url);
                if(url == null){
                    break;
                }
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = transformarUrlAbsoluta("http://" + new URI(url).getHost(), url); // Use uma baseUrl apropriada
                }
                if (!urlsProcessados.contains(url)) {
                    urlsProcessados.add(url);
                    processarPagina(url);
                }
                Thread.sleep(1000);

                //String[] textWithLines = Jsoup.parse(doc.html()).wholeText().split(" ");
                //for (String palavra : textWithLines) {
                    //palavra = palavra.trim();
                    //if(palavra.length()>3){
                        //System.out.println(palavra + " -> " + url);
                      //  gateway.addToIndex(palavra, url);
                    //}
                //}

                //System.out.println(doc);
                //Todo: Read JSOUP documentation and parse the html to index the keywords.
                //Then send back to server via index.addToIndex(...)
                //Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processarPagina(String url) {
        try{
            Document doc = Jsoup.connect(url).timeout(timeout).get();
            extrairLinks(doc);
            extrairpalavras(doc,url);
        }catch(IOException e){
            System.out.println("Erro"+ e.getMessage());
        }
    }

    public void extrairLinks(Document doc) throws RemoteException {
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
            //if (href.startsWith("/")) {
                //absoluteUrl = baseUrl.replaceAll("(https?://[^/]+).*", "$1") + href;
            //} else if (href.startsWith("http://") || href.startsWith("https://")) {
                //absoluteUrl = href;
            //} else {
                //String dominioBase = baseUrl.split("/")[0] + "//" + baseUrl.split("/")[2];
                //String caminhoAtual = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);
                //absoluteUrl = caminhoAtual + href;
            //}
            //gateway.putNew(absoluteUrl);
            //System.out.println("Link extraído: " + absoluteUrl);
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

    public void extrairpalavras(Document doc, String url) throws RemoteException {
        HashMap<String, HashSet<String>> index = new HashMap<>(); // Índice invertido local
        String[] palavras = doc.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
        for (String palavra : palavras) {
            if (palavra.length() > 3) { // Evita palavras curtas
                index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                gateway.addToIndex(palavra, url);
            }
        }
    }

@Override
    public void run() {
        executar();
    }

    public static void main(String[] args) {
        try {
            int numDownloaders=3;
            Thread[] threads = new Thread[numDownloaders];
            for (int i = 0; i < numDownloaders; i++) {
                Downloader d= new Downloader();
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