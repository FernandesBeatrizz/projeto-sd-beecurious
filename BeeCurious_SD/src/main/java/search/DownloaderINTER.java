package main.java.search;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Interface remota para o processamento de páginas web.
 *
 */
public interface DownloaderINTER extends Remote {

    /**
     * Processa o conteúdo de uma página web extraindo informações relevantes.
     *
     * @param url A URL da página web a ser processada.
     * @throws RemoteException Se houver um erro de comunicação remota ao processar a página.
     */
    void processarPagina(String url) throws java.rmi.RemoteException;
}
