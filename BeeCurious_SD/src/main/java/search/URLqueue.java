package main.java.search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;


/**
 * Classe responsável por gerir uma fila de URLs de forma concorrente e acessível remotamente via RMI.
 *
 * A {@code URLqueue} implementa a interface {@code QueueInterface} e utiliza uma {@code LinkedBlockingQueue}
 * para armazenar URLs. Suporta operações de inserção e remoção sincronizadas e permite o registo remoto
 * através do RMI.
 */
public class URLqueue extends UnicastRemoteObject implements QueueInterface {
    private LinkedBlockingQueue<String> urls;
    private final int max_size;
    private static final Logger LOGGER = Logger.getLogger(URLqueue.class.getName());
    private GatewayINTER gateway;


    /**
     * Constrói uma nova instância da URLqueue com um tamanho máximo para a queue.
     *
     * O construtor inicializa a fila e carrega os dados.
     *
     * @param max_size O tamanho máximo da fila de URLs.
     * @throws RemoteException Se ocorrer um erro de comunicação remota ao exportar o objeto.
     *
     */
    public URLqueue(int max_size) throws RemoteException{
        this.max_size=max_size;
        urls= new LinkedBlockingQueue<>(this.max_size);
    }

    /**
     * Adiciona um URL à fila.
     *
     * Se a queue não estiver cheia, o URL será adicionada à fila. Após adicionar o URL, a queue é salva.
     *
     * @param url URL a ser adicionada à fila.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public synchronized void putURL(String url) throws RemoteException{
        System.out.println("Tentar adicionar URL");
        if (urls.size() < max_size) {
            try {
                urls.put(url);
                LOGGER.info("URL adicionado: " + url);

            } catch (Exception e) {
                Thread.currentThread().interrupt();
                LOGGER.severe("Erro ao adicionar URL{} " + url);
            }
        } else {
            LOGGER.warning("A queue está cheia, logo o URL não pode ser adicionado");
        }
    }


    /**
     * Obtém e remove a próxima URL da fila.
     *
     * Este metodo bloqueia a execução até que um URL esteja disponível na fila. Se a fila estiver vazia,
     * o metodo aguarda até que um novo URL seja adicionado. Após remover o URL da fila, ela é marcada
     * como processada e a fila é salva no ficheiro.
     *
     * @return O próximo URL da fila.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     * @throws InterruptedException Se o thread for interrompido enquanto aguarda.
     *
     */
    public synchronized String getURL() throws RemoteException, InterruptedException {
        LOGGER.info("Solicitar um URL");

        // Bloqueia até que a fila tenha um elemento
        while (urls.isEmpty()) {
            try {
                System.out.println("Fila vazia, aguardando novos URLs...");
                wait(); // Bloqueia até que a fila tenha um elemento
            } catch (InterruptedException e) {
                LOGGER.warning("Erro: " + e.getMessage());
                throw e;
            }
        }

        // Retira a próxima URL da fila
        String url = urls.take();
        LOGGER.info("URL removida da fila: " + url);

        return url;
    }


    /**
     * Retorna o número de URLs presentes na fila.
     *
     * @return O tamanho da queue.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     *
     */
    public int getQueueSize() throws RemoteException {
        return urls.size();
    }

    /**
     * Retorna o tamanho máximo permitido para a queue.
     *
     * @return O tamanho máximo da fila.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public int getMaxSize() throws RemoteException {
        return max_size;
    }


    /**
     * Cria e regista uma nova instância da fila de URLs no registo RMI.
     *
     * Este metodo cria uma fila com o tamanho máximo de 1000 URLs e regista a fila no registo RMI
     * para que outros componentes possam acessá-la remotamente.
     *
     * @return A instância da {@code URLqueue} registada no RMI, ou {@code null} se ocorrer um erro.
     * @throws RemoteException Se ocorrer um erro de comunicação remota.
     */
    public static URLqueue createQueue() throws RemoteException {
        URLqueue queue = new URLqueue(1000);
        try {
            Registry registry = LocateRegistry.getRegistry("localHost", 8183);
            queue.gateway = (GatewayINTER) registry.lookup("Gateway");
            registry.rebind("URLqueue", queue);
            return queue;
        }catch (Exception e){
                e.printStackTrace();
                return null;
            }
    }

    /**
     * Metodo principal que cria e regista a fila no registro RMI.
     *
     * @param args Argumentos passados para o metodo principal.
     */
    public static void main(String[] args) {
        try {

            URLqueue queue = createQueue();
            queue.gateway.registerQueue(queue);
            System.out.println("-- queue check");

        } catch (RemoteException e) {
            System.err.println("Erro de comunicação remota: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
