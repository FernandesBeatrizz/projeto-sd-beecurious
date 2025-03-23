package main.search;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueueInterface extends Remote{
    void putURL(String url) throws RemoteException;
    String getURL() throws RemoteException, InterruptedException;
    String getUrlQueue() throws RemoteException, InterruptedException;
    int getQueueSize() throws RemoteException, InterruptedException;
    int getMaxSize() throws RemoteException, InterruptedException;
    void markURLAsProcessed(String url)throws RemoteException;
}
