package main.search;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;


/**
 * @throws RemoteException Se houver um erro de comunicação remota.
 */

public interface BarrelsINTER extends Remote {

    /**
     * Adiciona uma palavra ao índice associando-a a uma URL, título, citação e lista de links relacionados.
     */
    void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;

    /**
     * Busca todas as URLs associadas a uma palavra no índice.
     */
    List<String[]> searchWord(String word) throws RemoteException;

    /**
     * Indexa uma URL, processando seu conteúdo para extração de palavras-chave.
     */

    /**
     * Verifica se uma URL já foi indexada no sistema.
     */
    boolean containsURL(String url) throws RemoteException;

    /**
     * Retorna os 10 principais resultados para um termo de pesquisa.
     */
    List<String[]> top10(String termos) throws RemoteException;

    /**
     * Obtém uma lista de URLs associadas aos ponteiros de uma página específica.
     */
    List<String> obterpaginaurlponteiros(String url) throws RemoteException;

    void ping() throws RemoteException;

    void updateIndex(HashMap<String, ArrayList<String[]>> newIndex) throws RemoteException;

    void reviverBarrel() throws RemoteException;

    String getName() throws RemoteException;

    void mandarIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;


    /**
     * Atualiza o índice de pesquisa com novas associações entre palavras e URLs.
     */
}


