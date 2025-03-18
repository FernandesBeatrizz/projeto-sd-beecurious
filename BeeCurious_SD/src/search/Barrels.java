package search;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Barrels extends UnicastRemoteObject implements BarrelsINTER{
    private HashMap<String, ArrayList<String>> indiceParaPesquisas = new HashMap<>();
    private Gateway gateway;
    private int ip;
    private String nome;
    private int porta;


    public Barrels() throws RemoteException {
        super();
        this.indiceParaPesquisas= new HashMap<>();
        this.gateway.registerBarrel(this);
    }

    @Override
    public void addToIndex(String word, String url) throws RemoteException {
        indiceParaPesquisas.putIfAbsent(word, new ArrayList<>());
        if (!indiceParaPesquisas.get(word).contains(url)) {
            indiceParaPesquisas.get(word).add(url);
        }
    }

    @Override
    public synchronized List<String> searchWord(String word) throws RemoteException {
        return indiceParaPesquisas.getOrDefault(word, new ArrayList<>());
    }

    @Override
    public void syncWithReplica() throws RemoteException {
        gateway.syncBarrels(this);
    }

    public void updateIndex(HashMap<String, ArrayList<String>> newIndex) throws RemoteException {
        this.indiceParaPesquisas.clear();
        this.indiceParaPesquisas.putAll(newIndex);
    }

    //indexar
    //fazer a pesquisa
    //nao sei s é necessario guardar o que ja temos
    //nos barels temos de ver quando eles s deligam e dps fazer a conexao de novo com a gateway e ver se a informaçao é a mesma
}

//vao se ligar a gateway atraves d RMI callback
//têm de ter estas funçoes: fazer pesquisa e ...