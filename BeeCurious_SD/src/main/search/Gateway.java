package main.search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

/**
 * Classe que implementa o Gateway para comunicar entre clientes, barrels e downloaders.
 *
 */
public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    private final HashMap<String, ArrayList<String []>> indiceInvertido = new HashMap<>();
    private final ArrayList<BarrelsINTER> barrels;
    private int currentBarrelIndex = 0;
    private int currentDownloaderIndex = 0;
    private final Set<String> urlsIndexados= new HashSet<>();
    private QueueInterface urlQueue;
    private ArrayList<DownloaderINTER> downloaders;
    private final Timer syncTimer;
    /**
     * Construtor da classe Gateway.
     * Inicializa a fila de URLs e configura um temporizador para sincronização periódica dos barrels.
     *
     *
     */
    public Gateway() throws RemoteException {
        super();
        this.urlQueue=new URLqueue(1000);
        this.syncTimer = new Timer();
        this.syncTimer.scheduleAtFixedRate(new SyncTask(), 0, 6000);
        barrels = new ArrayList<>();
        downloaders = new ArrayList<>();
    }

    /**
     * Metodo principal para iniciar o Gateway e registá-lo no RMI Registry.
     *
     */
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

        Thread.sleep(5000);

        System.out.println("coreeee");

      } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    /**
     * Regista uma fila de URLs para a Gateway.
     *
     * @param queue A fila de URLs a ser registada.
     */
    public void registerQueue(QueueInterface queue) throws RemoteException {
        this.urlQueue = queue;
    }

    //CLIENTS  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Realiza a pesquisa de uma palavra no índice invertido.
     *
     * @param word Palavra a ser pesquisada.
     * @return Lista de URLs relacionadas à palavra pesquisada.
     */
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

    //BARRELS - - - - - - - - - - - - - - - - - - - - - - -

    public List<String[]> top10(String termos) throws RemoteException{
        return getBarrel().top10(termos);
    }

    private class SyncTask extends TimerTask {

        /**
         * Executa a sincronização dos barrels quando a tarefa é acionada.
         * Se ocorrer um erro de comunicação remota, ele será capturado e registado na consola.
         */
        @Override
        public void run() {
            try {
                syncBarrels();
                System.out.println("[Timer] Sincronização dos barrels check <3");
            } catch (RemoteException e) {
                System.err.println("[Timer] Erro ao sincronizar Barrels: " + e.getMessage());
            } catch (NullPointerException n){
                System.err.println("[Timer] Não há barrels conectados! :( " + n.getMessage());
            }
        }
    }

    /**
     * Obtém as páginas que apontam para um determinado URL.
     *
     * @param url O URL de interesse.
     * @return Lista de URLs que apontam para o URL fornecido.
     */
    public List<String> obterPaginasApontamPara(String url) throws RemoteException {
        List<String> paginasApontam = new ArrayList<>();

        for (BarrelsINTER barrel : barrels) {
            paginasApontam.addAll(barrel.obterpaginaurlponteiros(url));
        }

        return paginasApontam;
    }

    //REGISTAR E SINCRONIZAR BARRELS
    /**
     * Regista um novo Barrel no Gateway.
     *
     * @param barrel O Barrel a ser registado.
     */
    @Override
    public void registerBarrel(BarrelsINTER barrel) throws RemoteException {
        this.barrels.add(barrel);
        System.out.println("Barrel registado ");
    }

    /**
     * Remove um Barrel do Gateway.
     *
     * @param barrel O Barrel a ser removido.
     */
    @Override
    public void unregisterBarrel(BarrelsINTER barrel) throws RemoteException{
        if (barrels.contains(barrel)) {
            barrels.remove(barrel);
            System.out.println("Barrel removido");
        } else {
            System.out.println("Erro a remover barrel");
        }
    }

    /**
     * Sincroniza os barrels registando os índices atualizados.
     *
     */
    @Override
    public void syncBarrels() throws RemoteException {
        synchronized (indiceInvertido) {
            if (barrels.isEmpty()) {
                System.out.println("Não há barrels para sincronizar  :(");
                return;
            }
            if (barrels.size() == 1) {
                System.out.print("Só um barrel ligado, não é preciso sincronizar");
                return;
            }
            for (BarrelsINTER barrel : barrels) {
                try {
                    System.out.println("[DEBUG]\n");
                    barrel.ping();
                    barrel.updateIndex(indiceInvertido);
                } catch (RemoteException e) {
                    System.out.print("Erro a sincronizar barrel, barrel , a reviver barrel");
                    barrel.reviverBarrel();
                }
            }
        }
    }

    /**
     * Obtém um Barrel ativo do Gateway, alternando entre os disponíveis.
     *
     * @return Um objeto BarrelINTER ativo.
     */
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

    /**
     * Registra um downloader para processamento de URLs.
     *
     * @param downloader O downloader a ser registado.
     */
    public synchronized void registerDownloader (DownloaderINTER downloader){
        this.downloaders.add(downloader);
        System.out.println("Downloader registado ");
    }

    /**
     * Obtém um downloader disponível.
     *
     * @return Um objeto DownloaderINTER ativo.
     */
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

    /**
     * Adiciona uma nova URL para indexação.
     *
     * @param url A URL a ser adicionada.
     */
    public synchronized void putNew(String url) throws RemoteException {
        try {
            System.out.println("Verificar se o URL já foi indexado");
            if (!urlsIndexados.contains(url)) {
                if (urlQueue.getQueueSize() < urlQueue.getMaxSize()) {
                    System.out.println("Adicionar o URL a fila");
                    urlQueue.putURL(url);
                    urlsIndexados.add(url);
                    System.out.println("URL adicionado: " + url); // Log para verificação

                }else{
                    System.out.println("Queue cheia");
                    //wait();
                }
            } else {
                System.out.println("URL já adicionado: " + url);
            }
        } catch (RemoteException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Erro ao adicionar URL à fila: " + e.getMessage());
        }
    }

    /**
     * Adiciona uma palavra e o URL correspondente ao índice invertido.
     *
     * @param word A palavra a ser indexada.
     * @param url  A URL associada à palavra.
     */
    public void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException {
        String[] pagina = {url, titulo, citacao, String.join(",", links)};
        synchronized (indiceInvertido) {
            if (this.indiceInvertido.containsKey(word)) {
                if (!this.indiceInvertido.get(word).equals(pagina[0])) {
                    this.indiceInvertido.get(word).add(pagina);
                }
            } else {
                ArrayList<String[]> novaLista = new ArrayList<>();
                novaLista.add(pagina);
                this.indiceInvertido.put(word, novaLista);
            }
            syncBarrels();
        }
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

    /**
     * Retorna a fila de URLs do sistema.
     *
     * @return A interface da fila de URLs.
     */
    public QueueInterface getQueue() throws RemoteException {
        return this.urlQueue;
    }

}