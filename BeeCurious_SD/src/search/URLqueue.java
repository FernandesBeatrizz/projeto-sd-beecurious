package search;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Properties;
import java.io.IOException;


//usar o properties para o maximo da queue(ver o projeto d diogo)


public class URLqueue extends UnicastRemoteObject implements QueueInterface {
    private LinkedBlockingQueue<String> urls;
    private final int max_size;
    final private static String QUEUE_CONFIG = "queue.properties";
    

    public URLqueue(int maximo) throws RemoteException{
        this.max_size=maximo;
        this.urls= new LinkedBlockingQueue<>(this.max_size);
    }

    public synchronized void putURL(String url) throws RemoteException{
        if (urls.size() < max_size) {
            try {
                urls.add(url);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.out.println("Erro ao adicionar URL: " + e);
            }
        } else {
            System.out.println("A queue está cheia");
        }
    }
    public synchronized String getURL() throws RemoteException{
        //verificar se a queue está vazia
        while(urls.isEmpty()){
            try {
                wait();
            } catch (InterruptedException ex) {
                System.out.println("interrompida"+ ex);
            }
        }
        return urls.poll();
    }

    public static void main(String[] args) {
        try{
            Properties properties = new Properties();
            LOGGER.info("Loading \"{}\" file to acquire queue properties.", QUEUE_CONFIG);
            properties.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(QUEUE_CONFIG));
            int maximo = Integer.parseInt(properties.getProperty("max_size"));
        }catch(Exception e){
            System.out.println(e);
        }
    }
}
