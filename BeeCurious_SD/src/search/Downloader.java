package search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Downloader extends UnicastRemoteObject{
    //indexar_resultados() e novos_url() vao ser funçoes que temos d ter

    //takeNext()
    //responsavel por baixar as paginas e extrair palavras, links
    //funcionam em paralelo
    //mantem fila d URL

    //Index index;

    protected Downloader() throws RemoteException {
    }

    public void executar(){
        try {
            index = (Index) LocateRegistry.getRegistry(8183).lookup("index");
            index.registerClient(this);
            while (true) {
                String url = index.takeNext();
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
                    index.putNew(href);
                }

                String[] textWithLines = Jsoup.parse(doc.html()).wholeText().split(" ");
                for (String palavra : textWithLines) {
                    palavra = palavra.trim();
                    if(palavra.length()>3){
                        //System.out.println(palavra + " -> " + url);
                        index.addToIndex(palavra, url);
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
        Robot r;
        try {
            r = new Robot();
            r.executar();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    @Override
    public void printOnClient() throws RemoteException {
        System.out.println("Print on client");
    }
}

