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

    public URLqueue(int max_size) throws RemoteException{
        this.max_size=max_size;
        urls= new LinkedBlockingQueue<>(this.max_size);
        this.loadQueueFromFile();
    }

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

    private void saveQueueToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(caminhoficheiro))) {
            out.writeObject(urls);
            LOGGER.info("Fila de URLs guardada com sucesso.");
        } catch (IOException e) {
            LOGGER.warning("Erro ao guardar a fila de URLs: " + e.getMessage());
        }
    }

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

    public int getQueueSize() throws RemoteException {
        return urls.size();
    }

    public int getMaxSize() throws RemoteException {
        return max_size;
    }

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
