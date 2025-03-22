package main.search;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface BarrelsINTER extends Remote {

    // Adiciona uma palavra ao índice associado a uma URL
    void addToIndex(String word, String url, String titulo, String citacao, List<String>links) throws RemoteException;

    // Busca todas as URLs associadas a uma palavra
    List<String> searchWord(String word) throws RemoteException;

    // Sincronização entre réplicas (replica deve enviar o seu estado para outro Storage Barrel)
    void syncWithReplica() throws RemoteException;

    void updateIndex(HashMap<String, ArrayList<String>> indiceParaPesquisas) throws RemoteException;

    void indexarURL(String url) throws RemoteException;

    //void linksURLaddToIndex(String word, String url, String titulo, String citacao, List<String>links)  throws RemoteException;
}

