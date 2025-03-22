package main.search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    //vamos ter d meter aqui o nome d barrel p identificar o barrel p dps haver conexao
    //vamos ter d conectar com os barrels
    private LinkedList<String> listaParaFazerCrawl = new LinkedList<>();// mudei p queue
    private HashMap<String, ArrayList<String>> indiceInvertido = new HashMap<>();
    private ArrayList<BarrelsINTER> barrels;
    private ArrayList<DownloaderINTER> downloaders;
    private String url;
    private ClienteINTER cliente;
    private Set<String> urlsIndexados= new HashSet<>();
    private QueueInterface urlQueue;


    //private long counter = 0L;
    //private long timestamp = System.currentTimeMillis()

    public Gateway() throws RemoteException {
        super();
        this.cliente=null;
        this.urlQueue=new URLqueue(100);
        barrels = new ArrayList<>();
        downloaders = new ArrayList<>();
        //queue = null;
    }

    public static void main(String[] args) {
      try {
        Gateway gateway = new Gateway();
        String rmiName = "gateway";
        String rmiHost = "localhost";
        int rmiPort = 8183;

        System.setProperty("java.rmi.server.hostname", rmiHost);

        Registry registry = LocateRegistry.createRegistry(rmiPort);
        registry.rebind(rmiName, gateway);

        System.out.println("gateway ready. Waiting for input...");
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
        //return List.of();
        if(barrels.isEmpty()){
            return List.of();
        }
        BarrelsINTER barrel = barrels.get(new Random().nextInt(barrels.size())); //escolher um barrel aleatorio
        return barrel.searchWord(word);
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
        for (BarrelsINTER barrel : barrels) {
            barrel.updateIndex(indiceInvertido);
        }
    }


    //DOWNLOADRES - - - - - - - - - - - - - - - - - - -
    public synchronized String takeNext() throws RemoteException, InterruptedException {
        //return listaParaFazerCrawl.poll();
        return urlQueue.getURL();
    }

    public synchronized void putNew(String url) throws RemoteException {
        /*if (urlQueue.getQueueSize() >= urlQueue.getMaxSize()) {
            System.out.println("A fila está cheia! O URL não será adicionado: " + url);
            return;
        }
        if (!urlsIndexados.contains(url)) {
            urlQueue.putURL(url); //adiciona a fila
            urlsIndexados.add(url);
            System.out.println("URL adicionado: "+url);
        }else{
            System.out.println("URL já adicionado"+ url);
        }*/
        try {
            System.out.println("Verificar se o URL já foi indexado");
            if (!urlsIndexados.contains(url)) {  // Não precisamos dessa verificação se a fila já está controlando isso
                if (urlQueue.getQueueSize() < urlQueue.getMaxSize()) {
                    System.out.println("Adicionar o URL a fila");
                    urlQueue.putURL(url);  // Coloca o URL na fila
                    urlsIndexados.add(url); // Marca a URL como indexada
                    System.out.println("URL adicionado: " + url); // Log para verificação
                }else{
                    System.out.println("Queue cheia");
                }
            } else {
                System.out.println("URL já adicionado: " + url); // A URL já foi registrada antes
            }
        } catch (RemoteException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o estado de interrupção
            System.out.println("Erro ao adicionar URL à fila: " + e.getMessage());
        }
    }

    public synchronized void addToIndex(String word, String url) throws RemoteException {
        if (this.indiceInvertido.containsKey(word)) {
            if (!((ArrayList)this.indiceInvertido.get(word)).contains(url)) {
                this.indiceInvertido.get(word).add(url);
            }
        } else {
            ArrayList<String> novaLista = new ArrayList<>();
            novaLista.add(url);
            this.indiceInvertido.put(word, novaLista);
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
        if (this.indiceInvertido.containsKey(palavra)) {
            return this.indiceInvertido.get(palavra);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void registerClient(ClienteINTER cliente) throws RemoteException {

    }

    public QueueInterface getUrlQueue() throws RemoteException {
        return this.urlQueue;
    }

}