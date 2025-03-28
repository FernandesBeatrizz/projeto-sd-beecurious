package main.search;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * @throws RemoteException Se houver um erro de comunicação remota.
 */

public interface DownloaderINTER extends Remote {
    String get_url() throws java.rmi.RemoteException;
    void put_url(String url) throws java.rmi.RemoteException;

    /**
     * Processa o conteúdo de uma página web extraindo informações relevantes.
     *
     */
    void processarPagina(String url) throws java.rmi.RemoteException;
}
