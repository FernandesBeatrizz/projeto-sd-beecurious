package main.java.search;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * Interface remota para interagir com o sistema de indexação, downloads e filas.
 *
 */
public interface GatewayINTER extends Remote {

    // ========== CLIENTE ==========

    /**
     * Realiza a pesquisa por uma palavra-chave no índice de pesquisa.
     *
     * @param word A palavra-chave a ser pesquisada.
     * @return Uma lista de arrays de strings, cada um representando uma página encontrada no índice.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    List<String []> searchWord(String word) throws RemoteException;


    // ========== BARRELS ==========
    /**
     * Obtém todos os barrels (módulos de indexação) registrados no sistema.
     *
     * <p>Esse método retorna uma lista de todos os barrels que estão atualmente disponíveis para realizar
     * operações de indexação no sistema.</p>
     *
     * @return Uma lista de barrels registrados.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    List<BarrelsINTER> getAllBarrels() throws RemoteException;


    /**
     * Adiciona uma nova palavra ao índice, associando-a a uma URL e seus dados complementares.
     *
     * <p>Esse método permite que novos dados (palavra, URL, título, citação e links) sejam adicionados ao índice
     * para facilitar a pesquisa posterior.</p>
     *
     * @param word A palavra a ser adicionada ao índice.
     * @param url A URL associada à palavra.
     * @param titulo O título da página.
     * @param citacao A citação associada à página.
     * @param links A lista de links relacionados à página.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    public void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;



    /**
     * Registra um novo barrel (módulo de indexação) no sistema.
     *
     * @param barrel O barrel a ser registrado.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void registerBarrel(BarrelsINTER barrel) throws RemoteException;

    /**
     * Remove um barrel previamente registrado no sistema.
     *
     * @param barrel O barrel a ser removido.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void unregisterBarrel(BarrelsINTER barrel) throws RemoteException;

    /**
     * Obtém um barrel disponível no sistema.
     *
     * @return O barrel disponível.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    BarrelsINTER getBarrel() throws RemoteException;


    /**
     * Retorna uma lista de páginas que apontam para uma URL específica.
     *
     * @param url A URL para a qual se deseja obter as páginas que apontam para ela.
     * @return Uma lista de URLs de páginas que apontam para a URL fornecida.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    List<String> obterPaginasApontamPara(String url) throws RemoteException;


    // ========== DOWNLOADERS ==========
    /**
     * Adiciona uma nova URL à fila de processamento.
     *
     * @param var1 A URL a ser adicionada à fila.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void putNew(String var1) throws RemoteException;


    /**
     * Registra um downloader (processo responsável por baixar páginas) no sistema.
     *
     * @param downloader O downloader a ser registrado.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void registerDownloader(DownloaderINTER downloader) throws RemoteException;

    // ========== QUEUE ==========

    /**
     * Registra a fila de URLs no sistema para gerenciamento de downloads e indexação.
     *
     * @param queue A fila de URLs a ser registrada.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void registerQueue(QueueInterface queue) throws RemoteException;


    /**
     * Retorna a próxima URL a ser processada da fila.
     *
     * <p>Esse método obtém a próxima URL que está pronta para ser processada. A chamada pode bloquear até que
     * uma URL esteja disponível.</p>
     *
     * @return A próxima URL a ser processada.
     * @throws InterruptedException Se a thread for interrompida durante a espera.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    String getNextURL() throws InterruptedException, RemoteException;

    /**
     * Obtém a interface da fila de URLs para controlo de downloads e indexação.
     *
     * @return A interface da fila de URLs.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    QueueInterface getQueue() throws RemoteException;
}
