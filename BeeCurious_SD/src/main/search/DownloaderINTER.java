package main.search;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DownloaderINTER extends Remote {

    String get_url() throws java.rmi.RemoteException;
    void put_url(String url) throws java.rmi.RemoteException;
    void save_words(String word, String url) throws java.rmi.RemoteException;

    void processarPagina(String url) throws java.rmi.RemoteException;
}
