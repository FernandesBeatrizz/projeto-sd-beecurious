package main.java.meta2;

import main.java.search.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Esta classe funciona como intermediária.
 * Representa uma entidade que interage com o backend gateway remotamente por meio de RMI
 */
public class BackendRMIcliente {

    private final GatewayINTER gateway;


    /**
     * Construtor da classe BackendRMIcliente.
     * Estabelece uma ligação com o servidor RMI e obtém uma referência para a gateway.
     *
     * @throws RemoteException Se ocorrer uma falha na comunicação RMI.
     * @throws NotBoundException Se houver algum erro.
     * */
     public BackendRMIcliente() throws RemoteException, NotBoundException {
         // Conexão com o registo RMI na máquina local na porta 8183
         Registry registry = LocateRegistry.getRegistry("localhost", 8183);
         this.gateway = (GatewayINTER) registry.lookup("Gateway");
    }


    /**
     * Excuta uma pesquisa no sistema utilizando as palavras que forem fornecidas
     *
     * @param termos O termo que será pesquisado.
     * @return Uma lista de resultados da pesquisa.
     * @throws RemoteException Se ocorrer uma falha na comunicação RMI.
     */
    public List<String[]> search(String termos) throws RemoteException {
        return gateway.searchWord(termos);
    }


    /**
     * Indexa um novo URL
     *
     * @param url A URL a ser indexada.
     * @return A URL que foi indexada.
     * @throws RemoteException Se ocorrer uma falha na comunicação RMI.
     */
    public String indexarURL(String url) throws RemoteException {
        gateway.putNew(url);
        return url;
    }


    /**
     * Consulta todos os links que apontam para uma URL específica.
     *
     * @param url A URL da consulta.
     * @return Uma lista de URLs que apontam para a URL fornecida.
     * @throws RemoteException Se ocorrer uma falha na comunicação RMI.
     */
    public ArrayList<String> consultarLinks(String url) throws RemoteException {
        return new ArrayList<>(gateway.obterPaginasApontamPara(url));
    }


    /**
     * Obtém todas as stopwords do sistema.
     *
     * @return Uma string representando todas as stopwords do sistema.
     * @throws RemoteException Se ocorrer uma falha na comunicação RMI.
     */
    public String getStopWords() throws RemoteException {
        return gateway.getBarrel().getAllStopWords().toString();
    }
}