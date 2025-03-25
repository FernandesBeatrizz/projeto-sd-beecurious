package main.search;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GatewayINTER extends Remote {

    //cliente
    List<String> searchWord(String word) throws RemoteException;

    List<String> next_page() throws RemoteException;

    List<String> previous_page() throws RemoteException;

    List<String> links_para_url(String url) throws RemoteException;

    void printOnClient() throws RemoteException;


    //barrels
    void indexarURL(String url) throws RemoteException;

    void addLinksToURL(String url, List<String> links) throws RemoteException;

    void registerBarrel(BarrelsINTER barrel) throws RemoteException;

    void syncBarrels() throws RemoteException;

    QueueInterface getUrlQueue() throws RemoteException;

    void unregisterBarrel(BarrelsINTER barrel) throws RemoteException;

    BarrelsINTER getBarrel() throws RemoteException;

    List<String[]> top10(String termos) throws RemoteException;

    List<String> obterPaginasApontamPara(String url) throws RemoteException;


    //downloaders
    String takeNext() throws RemoteException, InterruptedException;

    void putNew(String var1) throws RemoteException;

    void markURLAsProcessed(String url) throws RemoteException;

    void addToIndex(String word, String url) throws RemoteException;

    String get_url() throws RemoteException;


    //caching
    void cacheSearchResults(String word, List<String> results) throws RemoteException;

    List<String> getCachedResults(String word) throws RemoteException;

    void registerClient(ClienteINTER cliente) throws RemoteException;

    //queue
    void registerQueue(QueueInterface queue) throws RemoteException;
}
