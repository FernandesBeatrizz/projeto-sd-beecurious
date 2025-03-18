package search;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownloaderINTER extends Remote {
    void printOnClient() throws RemoteException;

    public String get_url() throws java.rmi.RemoteException;
    public void put_url(String url) throws java.rmi.RemoteException;
    public void save_words(String word, String url) throws java.rmi.RemoteException;
}
