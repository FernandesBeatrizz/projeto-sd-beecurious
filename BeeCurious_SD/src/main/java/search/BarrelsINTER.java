package main.java.search;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Interface remota para a interação com os Barrels.
 *
 */
public interface BarrelsINTER extends Remote {

    /**
     * Adiciona uma palavra ao índice associando-a a uma URL, título, citação e lista de links relacionados.
     *
     * @param word Palavra a ser indexada.
     * @param url URL associada à palavra.
     * @param titulo Título da página.
     * @param citacao Citação da página.
     * @param links Lista de links relacionados à página.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void addToIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;

    /**
     * Verifica se uma URL já foi indexada no sistema.
     *
     * @param url A URL a ser verificada.
     * @return {@code true} se a URL já foi indexada, {@code false} caso contrário.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    boolean containsURL(String url) throws RemoteException;



    /**
     * VER OQ METER AQUI!!!!.
     *
     * @param termos O termo a ser pesquisado.
     * @return Uma lista de arrays de Strings, cada um representando uma página com o termo pesquisado.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    List<String[]> top10(String termos) throws RemoteException;


    /**
     * Obtém uma lista de URLs associadas aos ponteiros de uma página específica.
     *
     * @param url A URL da página cujos ponteiros devem ser recuperados.
     * @return Uma lista de URLs associadas aos ponteiros da página.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    List<String> obterpaginaurlponteiros(String url) throws RemoteException;

    /**
     * Verifica a conectividade do barrel.
     *
     * @throws RemoteException Se houver um erro ou se o barrel não estiver ativo.
     */
    void ping() throws RemoteException;


    /**
     * Obtém o nome do barrel.
     *
     * @return O nome do barrel.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    String getName() throws RemoteException;


    /**
     * Adiciona uma palavra ao índice associando-a a uma URL, título, citação e lista de links relacionados.
     *
     * @param word A palavra a ser indexada.
     * @param url A URL associada à palavra.
     * @param titulo O título da página.
     * @param citacao A citação da página.
     * @param links A lista de links relacionados.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void mandarIndex(String word, String url, String titulo, String citacao, List<String> links) throws RemoteException;

    /**
     * Registra uma ocorrência de palavra em uma URL, associando-a a um idioma.
     *
     * @param word A palavra que ocorreu.
     * @param url A URL associada à palavra.
     * @param language O idioma associado à palavra.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void registerWordOccurrence(String word, String url, String language) throws RemoteException;

    /**
     * Retorna o conjunto de stop words (palavras de parada) para um idioma específico.
     *
     * @param language O idioma para o qual as stop words devem ser retornadas.
     * @return Um conjunto de stop words para o idioma.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    Set<String> getStopWords(String language) throws RemoteException;


    /**
     * Atualiza a lista de stop words para um idioma específico.
     *
     * @param language O idioma cujas stop words devem ser atualizadas.
     * @param stopWords O novo conjunto de stop words.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void updateStopWords(String language, Set<String> stopWords) throws RemoteException;

    /**
     * Retorna todas as stop words de todos os idiomas.
     *
     * @return Um mapa com idiomas e seus respectivos conjuntos de stop words.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    Map<String, Set<String>> getAllStopWords() throws RemoteException;

    /**
     * Carrega os dados de índice e stop words para o barrel.
     *
     * @param indice O índice a ser carregado.
     * @param stopWords As stop words a serem carregadas.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    void carregarDados(Map<String, ArrayList<String[]>> indice, Map<String, Set<String>> stopWords) throws RemoteException;


    /**
     * Retorna o índice invertido do barrel.
     *
     * @return O índice invertido.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    Map<String, ArrayList<String[]>> getIndiceInvertido() throws RemoteException;
}



