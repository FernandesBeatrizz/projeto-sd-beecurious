package main.search;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * @throws RemoteException Se houver um erro de comunicação remota.
 */
public interface GatewayINTER extends Remote {

    // ========== CLIENTE ==========

    /**
     * Realiza a pesquisa por uma palavra-chave no índice de pesquisa.
     *
     */
    List<String []> searchWord(String word) throws RemoteException;

    // ========== BARRELS ==========

    public void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;
    /**
     * Registra um novo barrel (módulo de indexação) no sistema.
     *
     */
    void registerBarrel(BarrelsINTER barrel) throws RemoteException;

    /**
     * Obtém a interface da fila de URLs para controle de downloads e indexação.
     *
     */
    QueueInterface getQueue() throws RemoteException;

    /**
     * Remove um barrel previamente registrado no sistema.
     *
     */
    void unregisterBarrel(BarrelsINTER barrel) throws RemoteException;

    /**
     * Obtém um barrel disponível no sistema.
     *
     */
    BarrelsINTER getBarrel() throws RemoteException;

    /**
     * Obtém os 10 principais resultados para um termo de pesquisa.
     *
     */
    List<String[]> top10(String termos) throws RemoteException;

    /**
     * Retorna uma lista de páginas que apontam para uma URL específica.
     *
     */
    List<String> obterPaginasApontamPara(String url) throws RemoteException;


    // ========== DOWNLOADERS ==========
    /**
     * Adiciona uma nova URL à fila de processamento.
     *
     */
    void putNew(String var1) throws RemoteException;

    /**
     * Registra um downloader (processo responsável por baixar páginas) no sistema.
     *
     */
    void registerDownloader(DownloaderINTER downloader) throws RemoteException;


    // ========== CACHING ==========


    void cacheSearchResults(String word, List<String> results) throws RemoteException;


    List<String> getCachedResults(String word) throws RemoteException;

    // ========== QUEUE ==========

    /**
     * Registra a fila de URLs no sistema para gerenciamento de downloads e indexação.
     *
     */
    void registerQueue(QueueInterface queue) throws RemoteException;

    String getNextURL() throws InterruptedException, RemoteException;

    List<BarrelsINTER> getAllBarrels();
}
