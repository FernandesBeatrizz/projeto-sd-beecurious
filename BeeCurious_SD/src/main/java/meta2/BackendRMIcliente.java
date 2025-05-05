package main.java.meta2;

import main.java.search.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BackendRMIcliente {
    private final GatewayINTER gateway;

    public BackendRMIcliente() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 8183);
        this.gateway = (GatewayINTER) registry.lookup("Gateway");
    }

    public LinkedHashMap<String, String[]> search(String termos) throws RemoteException {
        return (LinkedHashMap<String, String[]>) gateway.searchWord(termos);
        /*List<String[]> results = gateway.searchWord(query);
        LinkedHashMap<String, String[]> formattedResults = new LinkedHashMap<>();

        for (String[] result : results) {
            formattedResults.put(result[0], result); // URL como chave
        }
        return formattedResults;*/
    }

    /**
     * Indexa um novo URL
     */
    public String indexarURL(String url) throws RemoteException {
        gateway.putNew(url);
        /*return "URL enviado para indexação: " + url;*/
        return url;
    }


    public ArrayList<String> consultarLinks(String url) throws RemoteException {
        return new ArrayList<>(gateway.obterPaginasApontamPara(url));
    }


    public String getStopWords() throws RemoteException {
        return gateway.getBarrel().getAllStopWords().toString();
    }
}