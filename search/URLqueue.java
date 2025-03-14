package search;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;



public class URLqueue extends UnicastRemoteObject implements QueueInterface {
    final private int max;  // tamanho maximo
    private Queue<String> queue; //ou ent usar LinkedBlockingQueue

    public URLqueue() throws RemoteException{
        this.max=10; //dps alterar o valor provavelmente
        this.queue= new LinkedList<>(); //ou entao usar LinkedBlockingQueue
    }

    public synchronized void putURL(String url) throws RemoteException{
        //dps ver s é maior que o maximo
        if (queue.size()>= max){
            System.out.println("Queue is full");
        }
        queue.add(url);
        notify();
    }
    public synchronized String getURL() throws RemoteException{
        //verificar se a queue está vazia
        while(queue.isEmpty()){
            try {
                wait();
            } catch (InterruptedException ex) {
                System.out.println("interrompida");
            }
        }
        return queue.poll();
    }
}
