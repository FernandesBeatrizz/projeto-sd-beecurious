package search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Downloader extends UnicastRemoteObject implements DownloaderINTER, Runnable {
    //indexar_resultados() e novos_url() vao ser funçoes que temos d ter

    //takeNext()
    //responsavel por baixar as paginas e extrair palavras, links
    //funcionam em paralelo
    //mantem fila d URL
    private static final int timeout = 5000;
    GatewayINTER gateway;
    private static final Set<String> urlsProcessados= new HashSet<>();

    protected Downloader() throws RemoteException {
    }

    public void executar(){
        try {
            Registry registry = LocateRegistry.getRegistry(8183);
            gateway = (GatewayINTER) registry.lookup("gateway");  // Certifique-se de que "gateway" é o nome correto
            gateway.registerClient((ClienteINTER) this);  //ver isto tbm
            while (true) {
                String url = gateway.takeNext();
                System.out.println(url);
                if(url == null){
                    break;
                }

                System.out.println(url);
                urlsProcessados.add(url);
                processarPagina(url);
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
            System.out.println("Erro");
        }
    }

    public void extrairLinks(Document doc) throws RemoteException {
        Elements anchors = doc.select("a");
        for(Element anchor : anchors) {
            String href = anchor.attr("href");
            System.out.println(href);
            gateway.putNew(href);
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

