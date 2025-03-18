package search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    //vamos ter d meter aqui o nome d barrel p identificar o barrel p dps haver conexao
    //vamos ter d conectar com os barrels
    private ArrayList<String> listaParaFazerCrawl = new ArrayList<>();
    private HashMap<String, ArrayList<String>> indiceParaPesquisas = new HashMap<>();
    private ArrayList<Barrels> lista_barrels;
    //private long counter = 0L;
    //private long timestamp = System.currentTimeMillis()

    public Gateway() throws RemoteException {
    super();   //nao sei s é preciso
    }

    public static void main(String[] args) {
      try {
        Gateway gateway = new Gateway();
        Registry registry = LocateRegistry.createRegistry(8183);
        registry.rebind("gateway", gateway);
        System.out.println("gateway ready. Waiting for input...");
        gateway.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");
        gateway.putNew("https://www.uc.pt");
        gateway.putNew("https://www.dn.pt");
        gateway.putNew("https://www.dn.pt");
         //Thread.sleep(4000L);
         //server.printOnClient();
        System.out.println("printed");
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }


    //CLIENTS  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public synchronized List<String> searchWord(String word) throws RemoteException {
        if (indiceParaPesquisas.containsKey(word)){
            System.out.println("Palavra encontrada: "+word);
            return indiceParaPesquisas.get(word);
        }else{
            System.out.println("Palavra não encontrada"+ word);
            return new ArrayList<>();
        }
        //return (List)(this.indiceParaPesquisas.containsKey(word) ? (List)this.indiceParaPesquisas.get(word) : new ArrayList());
    }

    public List<String> next_page() throws RemoteException{
        return new ArrayList<>();
    }

    public List<String> previous_page() throws RemoteException{
        return new ArrayList<>();
    }

    List<String> links_para_url(String url) throws RemoteException;


    public void printOnClient() {
        if (this.cliente != null) {
            try {
                this.cliente.printOnClient();
            } catch (RemoteException var2) {
            }
        }
    }

    //BARRELS - - - - - - - - - - - - - - - - - - - - - - -
    public void indexarURL(String url) throws RemoteException {
        System.out.println("URL indexado: " + url);
    }

    @Override
    public void addLinksToURL(String url, List<String> links) throws RemoteException {

    }

    public void registerBarrel(Barrels barrels) {
        if (this.lista_barrels == null) {
            this.lista_barrels = new ArrayList<>();
        }
        lista_barrels.add(barrels);
        System.out.println("Barrel registrado "); //nao sei s é preciso esta mensagem
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
        this.listaParaFazerCrawl.add(url);
        System.out.println("URL adicionado: "+url);
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
    public List<String> links_para_url(String url) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> guardaResultado(String palavra) throws RemoteException {
        if (this.indiceParaPesquisas.containsKey(palavra)) {
            return this.indiceParaPesquisas.get(palavra);
        } else {
            return new ArrayList<>();
        }
    }

}