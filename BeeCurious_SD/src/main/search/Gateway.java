package main.search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    private final HashMap<String, ArrayList<String>> indiceInvertido = new HashMap<>();
    private ArrayList<BarrelsINTER> barrels;
    private int currentBarrelIndex = 0;
    private int currentDownloaderIndex = 0;
    private final ClienteINTER cliente;
    private final Set<String> urlsIndexados= new HashSet<>();
    private QueueInterface urlQueue;
    private ArrayList<DownloaderINTER> downloaders;
    private final Timer syncTimer;

    public Gateway() throws RemoteException {
        super();
        this.cliente=null;
        this.urlQueue=new URLqueue(1000);
        this.syncTimer = new Timer();
        this.syncTimer.scheduleAtFixedRate(new SyncTask(), 0, 300000);
        barrels = new ArrayList<>();
        downloaders = new ArrayList<>();
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
        ArrayList<String> urls = new ArrayList<>();
        BarrelsINTER barrel = getBarrel();
            try {
                urls.addAll(barrel.searchWord(word));
                return urls;
            } catch (RemoteException error) {
                error.getMessage();
            }
        return urls;
    }

    public void printOnClient() {
        if (this.cliente != null) {
            try {
                this.cliente.printOnClient();
            } catch (RemoteException var2) {
                var2.printStackTrace();
            }
        }
    }

    //BARRELS - - - - - - - - - - - - - - - - - - - - - - -

    public List<String[]> top10(String termos) throws RemoteException{
        return barrels.get(0).top10(termos);
    }

    private class SyncTask extends TimerTask {
        @Override
        public void run() {
            try {
                syncBarrels();
                System.out.println("[Timer] Sincronização dos barrels check <3");
            } catch (RemoteException e) {
                System.err.println("[Timer] Erro ao sincronizar Barrels: " + e.getMessage());
            }
        }
    }


    public List<String> obterPaginasApontamPara(String url) throws RemoteException {
        List<String> paginasApontam = new ArrayList<>();

        for (BarrelsINTER barrel : barrels) {
            paginasApontam.addAll(barrel.obterpaginaurlponteiros(url));
        }

        return paginasApontam;
    }

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
        System.out.println();
    }


    public void processarQueue() throws RemoteException {
        while(true){
            try{
                String url= urlQueue.getURL();
                if (url!=null){
                    getDownloader().processarPagina(url);
                }else{
                    Thread.sleep(2000);
                }
            }catch (Exception e){
                Logger.getLogger("Erro"+ e.getMessage());
            }
        }
    }


    //REGISTAR E SINCRONIZAR BARRELS
    @Override
    public void registerBarrel(BarrelsINTER barrel) throws RemoteException {
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
    public synchronized void registerDownloader (DownloaderINTER downloader){
        this.downloaders.add(downloader);
        System.out.println("Downloader registado ");
    }
    public synchronized DownloaderINTER getDownloader() throws RemoteException{
        try{
            DownloaderINTER downloader = downloaders.get(currentDownloaderIndex);
            currentDownloaderIndex = (currentDownloaderIndex + 1) % downloaders.size();
            return downloader;
        }catch (Exception e){
            System.out.print("Erro"+ e.getMessage());
        }
        return null;
    }

    public synchronized String takeNext() throws RemoteException, InterruptedException {
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
            if (!this.indiceInvertido.get(word).contains(url)) {
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

    public QueueInterface getQueue() throws RemoteException {
        return this.urlQueue;
    }

}