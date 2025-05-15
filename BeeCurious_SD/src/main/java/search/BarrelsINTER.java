package main.java.search;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Obtém a lista das páginas mais relevantes que contêm todos os termos pesquisados.
     *
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
     * Retorna o conjunto de stop words (palavras de parada) para um idioma específico.
     *
     * @return Um conjunto de stop words para o idioma.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    Set<String> getStopWords() throws RemoteException;

    /**
     * Carrega os dados do índice invertido e a lista de palavras de paragem (stop words) para o sistema.
     *
     *
     * @param indice Mapa que representa o índice invertido, associando palavras a listas de ocorrências.
     * @param stopWords Conjunto de palavras de paragem a ser utilizado para filtragem.
     * @throws RemoteException Caso ocorra um erro na comunicação remota durante o carregamento dos dados.
     */
    void carregarDados(Map<String, ArrayList<String[]>> indice, Set<String>stopWords) throws RemoteException;


    /**
     * Retorna o índice invertido do barrel.
     *
     * @return O índice invertido.
     * @throws RemoteException Se houver um erro de comunicação remota.
     */
    Map<String, ArrayList<String[]>> getIndiceInvertido() throws RemoteException;

    /**
     * Atualiza a lista de palavras de stop words com o conjunto fornecido.
     *
     * @param newStopWords Conjunto de novas stop words a aplicar.
     * @throws RemoteException Caso ocorra um erro na comunicação remota durante a atualização.
     */
    void updateStopWords(Set<String> newStopWords) throws RemoteException;

    /**
     * Recebe um mapa de contagens de palavras para atualizar as estatísticas locais.
     *
     * @param contagens Mapa que associa palavras a contadores.
     * @throws RemoteException Caso ocorra um erro na comunicação remota durante a receção das contagens.
     */
    void receberContagemPalavras(Map<String, AtomicInteger> contagens) throws RemoteException;
}



