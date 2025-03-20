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


    public URLqueue(int maximo) throws RemoteException{
        this.max_size=10000;
        this.urls= new LinkedBlockingQueue<>(this.max_size);
    }

    public synchronized void putURL(String url) throws RemoteException{
        if (urls.size() < max_size) {
            try {
                urls.add(url);
                LOGGER.info("URL adicionado: " + url);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Erro ao adicionar URL: " + e);
            }
        } else {
            System.out.println("A queue está cheia, logo o URL não pode ser adicionado"+ url);
        }
    }
    public synchronized String getURL() throws RemoteException{
        //verificar se a queue está vazia
        while(urls.isEmpty()){
            try {
                wait();
            } catch (InterruptedException ex) {
                System.out.println("interrompida"+ ex);
                //Thread.currentThread().interrupt();
            }
        }
        String url = urls.poll();
        LOGGER.info("URL removida da fila: " + url);
        return url;
    }
    public static void main(String[] args) {
        try {
            // Cria uma instância da URLqueue com tamanho máximo de 10000
            URLqueue urlQueue = new URLqueue(10000);

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
}
