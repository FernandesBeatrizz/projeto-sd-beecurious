package main.java.search;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Classe que implementa o Gateway para comunicar entre clientes, barrels e downloaders.
 *
 */
public class Gateway extends UnicastRemoteObject implements GatewayINTER {
    private final ArrayList<BarrelsINTER> barrels;
    private int currentBarrelIndex = 0;
    private final Set<String> urlsIndexados = new HashSet<>();
    private QueueInterface urlQueue;
    private ArrayList<DownloaderINTER> downloaders;
    private Map<BarrelsINTER, String> barrelNames = new HashMap<>();

    /**
     * Construtor da classe Gateway.
     * Inicializa a fila de URLs e configura um temporizador para sincronização periódica dos barrels.
     */
    public Gateway() throws RemoteException {
        super();
        this.urlQueue = new URLqueue(1000);
        barrels = new ArrayList<>();
        downloaders = new ArrayList<>();
    }

    /**
     * Metodo principal para iniciar o Gateway e registá-lo no RMI Registry.
     */
    public static void main(String[] args) {
        try {
            Gateway gateway = new Gateway();
            String gatewayName = "Gateway";
            String gatewayHost = "localHost";
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
     * @param words Palavra a ser pesquisada.
     * @return Lista de URLs relacionadas à palavra pesquisada.
     */
    @Override
    public List<String[]> searchWord(String words) throws RemoteException {
        try {
            BarrelsINTER barrel = getBarrel(); // Vai tentar pingar e retornar
            if (barrel != null) {
                return barrel.top10(words); // Pode lançar RemoteException
            }
        } catch (RemoteException e) {
            System.out.println("Erro na pesquisa: " + e.getMessage());
            e.printStackTrace(); // Mostra onde a exceção foi realmente lançada
        }
        return new ArrayList<>();
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
        barrelNames.put(barrel, barrel.getName());
        System.out.println("Barrel registado: " + barrel.getName());

        // Se houver outros Barrels ativos, sincroniza o novo Barrel com um existente
        if (barrels.size() > 1) {
            try {
                // Escolhe um Barrel ativo (excluindo o recém-registado)
                BarrelsINTER barrelAtivo = barrels.stream()
                        .filter(b -> !b.equals(barrel))
                        .findFirst()
                        .orElse(null);

                if (barrelAtivo != null) {
                    System.out.println("A sincronizar o novo Barrel " + barrel.getName() + " com " + barrelAtivo.getName());

                    // Obtém o índice e as stop words do Barrel ativo
                    Map<String, ArrayList<String[]>> indice = barrelAtivo.getIndiceInvertido();
                    Map<String, Set<String>> stopWords = barrelAtivo.getAllStopWords();

                    // Envia para o novo Barrel
                    barrel.carregarDados(indice, stopWords);
                    System.out.println("Sincronização concluída.");
                }
            } catch (RemoteException e) {
                System.err.println("Erro ao sincronizar o novo Barrel: " + e.getMessage());
            }
        }
    }

    /**
     * Remove um Barrel do Gateway.
     *
     * @param barrel O Barrel a ser removido.
     */
    @Override
    public void unregisterBarrel(BarrelsINTER barrel) throws RemoteException {
        if (barrels.contains(barrel)) {
            barrels.remove(barrel);
            System.out.println("Barrel removido");
        } else {
            System.out.println("Erro a matar barrel");
        }
    }

    /**
     * Obtém um Barrel ativo do Gateway, alternando entre os disponíveis.
     *
     * @return Um objeto BarrelINTER ativo.
     */
    public BarrelsINTER getBarrel() throws RemoteException {
        if (barrels.isEmpty()) {
            throw new RemoteException("Nenhum barrel disponível");
        }

        int tentativas = 0;
        while (tentativas < barrels.size()) {
            BarrelsINTER barrel = barrels.get(currentBarrelIndex);
            currentBarrelIndex = (currentBarrelIndex + 1) % barrels.size();
            try {
                System.out.println("a ir ao ping do barrel: " + barrel.getName());
                barrel.ping(); // Deve lançar RemoteException se o processo morreu
                System.out.println("Barrel ativo: " + barrel.getName());
                return barrel;
            } catch (RemoteException e) {
                System.out.println("Barrel inativo");
                try {
                    matarBarrel(barrel);
                } catch (Exception ex) {
                    System.out.println("Erro ao remover barrel: " + ex.getMessage());
                }
                tentativas++;
            }
        }
        throw new RemoteException("Todos os barrels estão indisponíveis");
    }

    public void matarBarrel(BarrelsINTER barrel) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("localHost", 8183);
        try {
            String barrel_nome = barrelNames.get(barrel);
            if (barrel_nome != null) {
                try {
                    registry.unbind(barrel_nome);
                } catch (NotBoundException e) {
                    System.out.println("Barrel já não estava registado: " + barrel_nome);
                }
                unregisterBarrel(barrel);
                barrelNames.remove(barrel);
                System.out.println("Barrel " + barrel_nome + " removido do RMI Registry.");
            } else {
                System.out.println("Nome do barrel não encontrado para: " + barrel);
            }
        } catch (RemoteException e) {
            System.out.println("Erro ao aceder ao RMI Registry: " + e.getMessage());
        }
    }

    @Override
    public List<BarrelsINTER> getAllBarrels() {

        return barrels;
    }

    //DOWNLOADRES - - - - - - - - - - - - - - - - - - -

    /**
     * Registra um downloader para processamento de URLs.
     *
     * @param downloader O downloader a ser registado.
     */
    public synchronized void registerDownloader(DownloaderINTER downloader) {
        this.downloaders.add(downloader);
        System.out.println("Downloader registado ");
    }

    /**
     * Adiciona uma nova URL para indexação.
     *
     * @param url A URL a ser adicionada.
     */
    public synchronized void putNew(String url) throws RemoteException {
        try {
            if (!urlsIndexados.contains(url)) {
                if (urlQueue.getQueueSize() < urlQueue.getMaxSize()) {
                    urlQueue.putURL(url);
                    urlsIndexados.add(url);
                    System.out.println("URL adicionado: " + url); // Log para verificação

                } else {
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
    @Override
    public void addToIndex(String word, String url, String titulo, String citacao, List<String> links)
            throws RemoteException {

        // Obtém um Barrel disponível
        BarrelsINTER barrel = getBarrel();
        if (barrel != null) {
            // Envia diretamente para o Barrel
            barrel.addToIndex(word, url, titulo, citacao, links);
        } else {
            System.err.println("Nenhum Barrel disponível para indexação!");
        }
    }

    //QUEUE - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Retorna a fila de URLs do sistema.
     *
     * @return A interface da fila de URLs.
     */
    public QueueInterface getQueue() throws RemoteException {
        return this.urlQueue;
    }

    public synchronized String getNextURL() throws InterruptedException, RemoteException {
        if (urlQueue.getQueueSize() == 0) {
            return null; // Fila vazia
        }
        String url = urlQueue.getURL(); // Remove da fila
        urlsIndexados.add(url); // Marca como processado
        return url;
    }
}
