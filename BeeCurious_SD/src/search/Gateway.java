package search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    //vamos ter d meter aqui o nome d barrel p identificar o barrel p dps haver conexao
    //vamos ter d conectar com os barrels
    private ArrayList<String> listaParaFazerCrawl = new ArrayList<>();
    private HashMap<String, ArrayList<String>> indiceParaPesquisas = new HashMap<>();
    private ArrayList<BarrelsINTER> barrels;
    private ArrayList<DownloaderINTER> downloaders;
    private QueueInterface queue;
    private String url;
    private ClienteINTER cliente;
    private Set<String> urlsIndexados= new HashSet<>();
    //private long counter = 0L;
    //private long timestamp = System.currentTimeMillis()

    public Gateway() throws RemoteException {
        super();
        this.cliente=null;
        barrels = new ArrayList<>();
        downloaders = new ArrayList<>();
        queue = null;
    }

    public static void main(String[] args) {
      try {
        Gateway gateway = new Gateway();
        String rmiName = "Gateway";
        String rmiHost = " localHost";
        int rmiPort = 8183;

        System.setProperty("java.rmi.server.hostname", rmiHost);

        Registry registry = LocateRegistry.createRegistry(rmiPort);
        registry.rebind(rmiName, gateway);

        System.out.println("gateway ready. Waiting for input...");
        //gateway.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");
        //gateway.putNew("https://www.uc.pt");
        //gateway.putNew("https://www.dn.pt");
        //gateway.putNew("https://www.dn.pt");
         //Thread.sleep(4000L);
         //server.printOnClient();

          System.out.println("printed");
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }


    //CLIENTS  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public synchronized List<String> searchWord(String word) throws RemoteException {
        /*ArrayList<String> urls = new ArrayList<>(String);
        for (BarrelsINTER barrel : barrels) {
            try {
                urls.putAll(barrel.searchWord(word);
                return urls;
            } catch (RemoteException error) {
                //barrel morreu;
            }*/
        return List.of();
    }

    public List<String> next_page() throws RemoteException{
        return new ArrayList<>();
    }

    public List<String> previous_page() throws RemoteException{
        return new ArrayList<>();
    }

    public List<String> links_para_url(String url) throws RemoteException{
        this.url = url;
        return new ArrayList<>(); //nao percebo s é isto
    }


    public void printOnClient() {
        if (this.cliente != null) {
            try {
                this.cliente.printOnClient();
            } catch (RemoteException var2) {
                var2.printStackTrace();
            }
        }//acho que devemos meter um else, ou nao
    }

    //BARRELS - - - - - - - - - - - - - - - - - - - - - - -
    public void indexarURL(String url) throws RemoteException {
        System.out.println("URL indexado: " + url);
    }

    @Override
    public void addLinksToURL(String url, List<String> links) throws RemoteException {

    }

    @Override
    public void registerBarrel(BarrelsINTER barrel) throws RemoteException {
        if (this.barrels == null) {
            this.barrels = new ArrayList<>();
        }
        this.barrels.add(barrel);
        System.out.println("Barrel registado "); //nao sei s é preciso esta mensagem
    }

    @Override
    public void syncBarrels() throws RemoteException {

    }

    //DOWNLOADRES - - - - - - - - - - - - - - - - - - -
    public synchronized String takeNext() throws RemoteException {
        if (!this.listaParaFazerCrawl.isEmpty()) {
            String s = (String)this.listaParaFazerCrawl.get(0);
            this.listaParaFazerCrawl.remove(0);
            return s;
        } else {
            return null;
        }
    }

    public synchronized void putNew(String url) throws RemoteException {
        if (!urlsIndexados.contains(url)) {
            listaParaFazerCrawl.add(url);
            urlsIndexados.add(url);
            System.out.println("URL adicionado: "+url);
        }else{
            System.out.println("URL já adicionado"+ url);
        }
    }

    public synchronized void addToIndex(String word, String url) throws RemoteException {
        if (this.indiceParaPesquisas.containsKey(word)) {
            if (!((ArrayList)this.indiceParaPesquisas.get(word)).contains(url)) {
                this.indiceParaPesquisas.get(word).add(url);
            }
        } else {
            ArrayList<String> novaLista = new ArrayList<>();
            novaLista.add(url);
            this.indiceParaPesquisas.put(word, novaLista);
        }
    }

    @Override
    public String get_url() throws RemoteException {
        return "";
    }

    //CACHING - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void cacheSearchResults(String word, List<String> results) throws RemoteException {

    }

    @Override
    public List<String> getCachedResults (String word) throws RemoteException {
        return List.of();
    }

    //RANDOM - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


    @Override
    public List<String> guardaResultado(String palavra) throws RemoteException {
        if (this.indiceParaPesquisas.containsKey(palavra)) {
            return this.indiceParaPesquisas.get(palavra);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void registerClient(ClienteINTER cliente) throws RemoteException {

    }

}