package main.search;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueueInterface extends Remote{
    void putURL(String url) throws RemoteException;
    String getURL() throws RemoteException, InterruptedException;
}
