package search;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BarrelsINTER extends Remote {

    // Adiciona uma palavra ao índice associado a uma URL
    void addToIndex(String word, String url) throws RemoteException;

    // Busca todas as URLs associadas a uma palavra
    List<String> searchWord(String word) throws RemoteException;

    // Sincronização entre réplicas (replica deve enviar o seu estado para outro Storage Barrel)
    void syncWithReplica() throws RemoteException;

}

