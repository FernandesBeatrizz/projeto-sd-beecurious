package search;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Index extends Remote {
    void indexarURL(String url) throws RemoteException;
    List<String> pesquisar(String var1) throws RemoteException;
    List<String> next_page() throws RemoteException;
    List<String> previous_page() throws RemoteException;
    List<String> links_para_url(String url) throws RemoteException;
    void addToIndex(String word, String url) throws RemoteException;
    //void addLinksToURL(String url, List<String> links) throws RemoteException;
    //String takeNext() throws RemoteException;
    //void putNew(String var1) throws RemoteException;
    //boolean registerClient(cliente var1) throws RemoteException;
}



//metodos que o motor de busca deve implementar, todos têm que ser remotos por causa das operaçoes RMI
