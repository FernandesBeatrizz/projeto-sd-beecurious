package main.search;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;



public class URLqueue extends UnicastRemoteObject implements QueueInterface {
    private static LinkedBlockingQueue<String> urls;
    private final int max_size;
    //final private static String QUEUE_CONFIG = "queue.properties";
    private static final Logger LOGGER = Logger.getLogger(URLqueue.class.getName());
    private static final String caminhoficheiro = "queue.obj";
    private GatewayINTER gateway;


    /**
     * Constrói uma nova instância da URLqueue com um tamanho máximo para a queue.
     *
     * O construtor inicializa a fila e carrega os dados persistidos do arquivo, se houver.
     *
     * @param max_size O tamanho máximo da fila de URLs.
     */
    public URLqueue(int max_size) throws RemoteException{
        this.max_size=max_size;
        urls= new LinkedBlockingQueue<>(this.max_size);
        this.loadQueueFromFile();
    }

    /**
     * Adiciona uma URL à fila.
     *
     * Se a queue não estiver cheia, a URL será adicionada à fila. Caso contrário, um aviso será gerado
     * informando que a queue está cheia. Após adicionar a URL, a queue é salva.
     *
     * @param url A URL a ser adicionada à fila.
     */
    public synchronized void putURL(String url) throws RemoteException{
        System.out.println("Tentar adicionar URL");
        if (urls.size() < max_size) {
            try {
                urls.put(url);
                LOGGER.info("URL adicionado: " + url);
                saveQueueToFile();
                notifyAll();

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
     * Este metodo bloqueia a execução até que uma URL esteja disponível na fila. Se a fila estiver vazia,
     * o metodo aguarda até que uma nova URL seja adicionada. Após remover a URL da fila, ela é marcada
     * como processada e a fila é salva em disco.
     *
     * @return A próxima URL da fila.
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
        markURLAsProcessed(url);

        return url;
    }

    /**
     * Guarda o estado atual da queue.
     *
     * Este metodo serializa a fila de URLs e escreve-a em um arquivo, garantindo
     * que as URLs adicionadas ou removidas possam ser recuperadas na próxima execução.
     */
    private void saveQueueToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(caminhoficheiro))) {
            out.writeObject(urls);
            LOGGER.info("Fila de URLs guardada com sucesso.");
        } catch (IOException e) {
            LOGGER.warning("Erro ao guardar a fila de URLs: " + e.getMessage());
        }
    }

    /**
     * Carrega o estado da queu do arquivo.
     *
     * Caso o arquivo exista, a queue é carregada a partir dele. Isso permite que
     * a queue seja restaurada de seu estado anterior após uma reinicialização.
     */
    private void loadQueueFromFile() {
        if (new File(caminhoficheiro).exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(caminhoficheiro))) {
                LinkedBlockingQueue<String> filaCarregada = (LinkedBlockingQueue<String>) in.readObject();
                urls.addAll(filaCarregada);
                LOGGER.info("Fila de URLs carregada com sucesso.");
            } catch (Exception e) {
                LOGGER.warning("Erro ao carregar fila de URLs: " + e.getMessage());
            }
        }
    }

    /**
     * Marca o URL como processado e remove-a da fila.
     *
     * Esse metodo remove a URL da fila e a salva em disco. Ele é utilizado após a URL ser processada.
     *
     * @param url URL que vai ser marcado como processado.
     */
    public synchronized void markURLAsProcessed(String url) throws RemoteException {
        if (urls.remove(url)) {
            LOGGER.info("URL processado e removido da fila: " + url);
            saveQueueToFile();
            notifyAll();
        } else {
            LOGGER.warning("URL não encontrado na fila: " + url);
        }
    }



    @Override
    public String getUrlQueue() throws RemoteException, InterruptedException {
        return this.toString();
    }

    /**
     * Retorna o número de URLs presentes na fila.
     *
     * @return O tamanho da queue.
     */
    public int getQueueSize() throws RemoteException {
        return urls.size();
    }

    /**
     * Retorna o tamanho máximo permitido para a queue.
     *
     * @return O tamanho máximo da fila.
     */
    public int getMaxSize() throws RemoteException {
        return max_size;
    }


    /**
     * Cria e registra uma nova instância da fila de URLs no registro RMI.
     *
     * Esse metodo cria uma fila com o tamanho máximo de 1000 URLs e registra a fila no registro RMI
     * para que outros componentes possam acessá-la remotamente.
     *
     * @return A instância da URLqueue registrada no RMI.
     */
    public static URLqueue createQueue() throws RemoteException {
        URLqueue queue = new URLqueue(1000);
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 8183);
            queue.gateway = (GatewayINTER) registry.lookup("Gateway");
            registry.rebind("URLqueue", queue);
            return queue;
        }catch (Exception e){
                e.printStackTrace();
                return null;
            }
    }


    /**
     * Metodo principal que cria e registra a fila no registro RMI.
     *
     * Este metodo é responsável por inicializar e registrar a queue de URLs no registro RMI para que outros
     * componentes possam acessá-la remotamente.
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
