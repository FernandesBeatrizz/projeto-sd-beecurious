package main.java.search;
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

    String getName() throws RemoteException;

    void mandarIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;

    void registerWordOccurrence(String word, String url, String language) throws RemoteException;
    Set<String> getStopWords(String language) throws RemoteException;
    void updateStopWords(String language, Set<String> stopWords) throws RemoteException;
    Map<String, Set<String>> getAllStopWords() throws RemoteException;
    void carregarDados(Map<String, ArrayList<String[]>> indice, Map<String, Set<String>> stopWords) throws RemoteException;
    Map<String, ArrayList<String[]>> getIndiceInvertido() throws RemoteException;
}



