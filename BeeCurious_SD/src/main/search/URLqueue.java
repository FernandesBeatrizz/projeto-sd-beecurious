package main.search;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;



//usar o properties para o maximo da queue(ver o projeto d diogo)


public class URLqueue extends UnicastRemoteObject implements QueueInterface {
    private LinkedBlockingQueue<String> urls;
    private final int max_size;
    //final private static String QUEUE_CONFIG = "queue.properties";
    private static final Logger LOGGER = Logger.getLogger(URLqueue.class.getName());


    public URLqueue(int max_size) throws RemoteException{
        this.max_size=max_size;
        this.urls= new LinkedBlockingQueue<>(this.max_size);
    }

    public synchronized void putURL(String url) throws RemoteException{
        if (urls.size() < max_size) {
            try {
                urls.put(url);
                LOGGER.info("URL adicionado: " + url);
                notifyAll();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Erro ao adicionar URL: " + e);
            }
        } else {
            System.out.println("A queue está cheia, logo o URL não pode ser adicionado"+ url);
        }
    }
    public synchronized String getURL() throws RemoteException, InterruptedException {
        /*try {
            String url = urls.take(); // Retira o próximo URL da fila
            LOGGER.info("URL removido da fila: " + url); // Log para depuração
            return url;
        } catch (InterruptedException e) {
            LOGGER.warning("Thread interrompida ao pegar URL: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
            throw new RemoteException("Erro ao pegar URL da fila", e);
        }*/
        /*if (urls.isEmpty()) {
            System.out.println("Fila está vazia! Nenhum URL para processar.");
            return null;  // Em vez de bloquear, retorna null
        }*/
        //return urls.take();
        while (urls.isEmpty()) {
            System.out.println("Fila vazia, a espera de novos URLs...");
            wait(); // Bloqueia até que a fila tenha um elemento
        }
        String url = urls.take(); // Retira a próxima URL da fila
        LOGGER.info("URL removida da fila: " + url); // Log para depuração
        return url;
    }

    @Override
    public String getUrlQueue() throws RemoteException, InterruptedException {
        return this.toString();
    }

    public static void main(String[] args) {
        try {
            // Cria uma instância da URLqueue com tamanho máximo de 10000
            URLqueue urlQueue = new URLqueue(10);

            // Cria ou obtém o RMI Registry na porta padrão (1099)
            Registry registry = LocateRegistry.createRegistry(1099);

            // Registra o objeto URLqueue no RMI Registry com o nome "URLqueue"
            registry.bind("URLqueue", urlQueue);

            System.out.println("URLqueue registrado no RMI Registry e pronto para uso.");
        } catch (RemoteException e) {
            System.err.println("Erro de comunicação remota: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getQueueSize() throws RemoteException {
        return urls.size();
    }

    public int getMaxSize() throws RemoteException {
        return max_size;
    }


}
