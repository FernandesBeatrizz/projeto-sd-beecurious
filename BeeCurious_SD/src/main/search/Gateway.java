package main.search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    private HashMap<String, ArrayList<String>> indiceInvertido = new HashMap<>();
    private ArrayList<BarrelsINTER> barrels;
    private int currentBarrelIndex = 0;
    private String url;
    private ClienteINTER cliente;
    private Set<String> urlsIndexados= new HashSet<>();
    private QueueInterface urlQueue;
    //private Timer syncTimer;
    //private long counter = 0L;
    //private long timestamp = System.currentTimeMillis()

    public Gateway() throws RemoteException {
        super();
        this.cliente=null;
        this.urlQueue=new URLqueue(1000);
        //this.syncTimer = new Timer();
        //this.syncTimer.scheduleAtFixedRate(new SyncTask(), 0, 300000);
        barrels = new ArrayList<>();
    }

    public static void main(String[] args) {
      try {
        Gateway gateway = new Gateway();
        String gatewayName = "Gateway";
        String gatewayHost = "localhost";
        int gatewayPort = 8183;

        System.setProperty("java.rmi.server.hostname", gatewayHost);

        Registry registry = LocateRegistry.createRegistry(gatewayPort);
        registry.rebind(gatewayName, gateway);

        System.out.println("gateway ready. Waiting for input...");
         //Thread.sleep(4000L);
         //server.printOnClient();


          System.out.println("printed");
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public void registerQueue(QueueInterface queue) throws RemoteException {
        this.urlQueue = queue;
    }

    //CLIENTS  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public synchronized List<String> searchWord(String word) throws RemoteException {
        ArrayList<String> urls = new ArrayList<String>();
        BarrelsINTER barrel = getBarrel();
            try {
                urls.addAll(barrel.searchWord(word));
                return urls;
            } catch (RemoteException error) {
                error.getMessage();
            }
        return urls;
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

    public List<String[]> top10(String termos) throws RemoteException{
        if(barrels.isEmpty()){
            throw new RemoteException("Nenhum barrel");
        }
        return barrels.get(0).top10(termos);
    }

    /*private class BarrelsSyn extends TimerTask {
        @Override
        public void run() {
            try {
                syncBarrels();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }*/

    //RELACIONADO A URLS
    public void indexarURL(String url) throws RemoteException {
        System.out.println("URL recebido para indexação: " + url);
        try{
            urlQueue.putURL(url);
            System.out.println("URL adicionado a fila d gateway");
        }catch (Exception e){
            System.out.print("Erro"+ e.getMessage());
            return;
        }

        if (barrels != null){
            for(BarrelsINTER barrel : barrels){
                try{
                    barrel.indexarURL(url);
                    System.out.println("URL enviado p o barrel");
                }catch(RemoteException e){
                    System.out.print("Erro"+ e.getMessage());
                }
            }
        }
    }

    @Override
    public void addLinksToURL(String url, List<String> links) throws RemoteException {
        System.out.println("");
    }


    //REGISTAR E SINCRONIZAR BARRELS
    @Override
    public void registerBarrel(BarrelsINTER barrel) throws RemoteException {
        if (this.barrels == null) {
            this.barrels = new ArrayList<>();
        }
        this.barrels.add(barrel);
        System.out.println("Barrel registado "); //nao sei s é preciso esta mensagem
    }

    @Override
    public void unregisterBarrel(BarrelsINTER barrel) throws RemoteException{
        if (barrels.contains(barrel)) {
            barrels.remove(barrel);
            System.out.println("Barrel removido");
        } else {
            System.out.println("Erro a remover barrel");
        }
    }

    @Override
    public void syncBarrels() throws RemoteException {
        for (BarrelsINTER barrel : barrels) {
            barrel.updateIndex(indiceInvertido);
        }
    }

    @Override
    public QueueInterface getUrlQueue() throws RemoteException {
        return this.urlQueue;
    }

    public BarrelsINTER getBarrel() throws RemoteException{
        try{
            BarrelsINTER barrel = barrels.get(currentBarrelIndex);
            currentBarrelIndex = (currentBarrelIndex + 1) % barrels.size();
        return barrel;
        }catch (Exception e){
            System.out.print("Erro"+ e.getMessage());
        }
        return null;
    }


    //DOWNLOADRES - - - - - - - - - - - - - - - - - - -
    public synchronized String takeNext() throws RemoteException, InterruptedException {
        //return listaParaFazerCrawl.poll();
        return urlQueue.getURL();
    }

    public synchronized void putNew(String url) throws RemoteException {
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
                    //wait();
                }
            } else {
                System.out.println("URL já adicionado: " + url); // A URL já foi registrada antes
            }
        } catch (RemoteException | InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o estado de interrupção
            System.out.println("Erro ao adicionar URL à fila: " + e.getMessage());
        }
    }

    @Override
    public synchronized void markURLAsProcessed(String url) throws RemoteException {
        urlQueue.markURLAsProcessed(url);
        notifyAll();
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
        syncBarrels();
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
    public void registerClient(ClienteINTER cliente) throws RemoteException {

    }

}