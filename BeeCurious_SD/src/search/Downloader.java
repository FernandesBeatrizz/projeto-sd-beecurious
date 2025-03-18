package search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Downloader extends UnicastRemoteObject implements DownloaderINTER {
    //indexar_resultados() e novos_url() vao ser funçoes que temos d ter

    //takeNext()
    //responsavel por baixar as paginas e extrair palavras, links
    //funcionam em paralelo
    //mantem fila d URL

    GatewayINTER gateway;

    protected Downloader() throws RemoteException {
    }

    public void executar(){
        try {
            Registry registry = LocateRegistry.getRegistry(8183);
            gateway = (GatewayINTER) registry.lookup("gateway");  // Certifique-se de que "gateway" é o nome correto
            //gateway = (GatewayINTER) LocateRegistry.getRegistry(8183).lookup("index");   //ver o locateRegistry
            gateway.registerClient((Cliente) this);  //ver isto tbm
            while (true) {
                String url = gateway.takeNext();
                System.out.println(url);
                if(url == null){
                    break;
                }

                //System.out.println(url);
                Document doc = Jsoup.connect(url).get();
                Elements anchors = doc.select("a");
                for (Element anchor : anchors) {
                    String href = anchor.attr("href");
                    //System.out.println(href);
                    gateway.putNew(href);
                }

                String[] textWithLines = Jsoup.parse(doc.html()).wholeText().split(" ");
                for (String palavra : textWithLines) {
                    palavra = palavra.trim();
                    if(palavra.length()>3){
                        //System.out.println(palavra + " -> " + url);
                        gateway.addToIndex(palavra, url);
                    }
                }

                //System.out.println(doc);
                //Todo: Read JSOUP documentation and parse the html to index the keywords.
                //Then send back to server via index.addToIndex(...)
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Downloader d;
        try {
            d = new Downloader();
            d.executar();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

