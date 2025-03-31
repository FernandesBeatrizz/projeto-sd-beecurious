package main.search;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota para a queue de URLs.
 *
 * Essa interface define os métodos que permitem adicionar, recuperar e gerenciar URLs. Como esta interface estende {@link Remote},
 * os seus métodos podem ser chamados remotamente via RMI.
 *
 */
public interface QueueInterface extends Remote{

    /**
     * Adiciona uma URL à fila.Este metodo permite que clientes remotos adicionem uma URL à queue para processamento posterior.
     *
     * @param url A URL a ser adicionada à fila.
     */
    void putURL(String url) throws RemoteException;

    /**
     * Obtém e remove a próxima URL disponível na queue.
     *
     * @return A próxima URL disponível na fila.
     */
    String getURL() throws RemoteException, InterruptedException;



    /**
     * Retorna o número de URLs presentes atualmente na fila.
     *
     * @return O tamanho da fila de URLs.
     */
    int getQueueSize() throws RemoteException, InterruptedException;

    /**
     * Retorna o tamanho máximo permitido para a fila.
     *
     * @return O tamanho máximo da fila.
     */
    int getMaxSize() throws RemoteException, InterruptedException;


    /**
     * Marca uma URL como processada e a remove da queue.
     *
     * @param url A URL a ser marcada como processada.
     */
    void markURLAsProcessed(String url)throws RemoteException;
}
