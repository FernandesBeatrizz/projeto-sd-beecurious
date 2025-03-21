package main.search;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Barrels extends UnicastRemoteObject implements BarrelsINTER{
    private HashMap<String, ArrayList<String>> indiceParaPesquisas = new HashMap<>();
    private GatewayINTER gateway;
    //private int ip; host??
    private String name;
    private int port;

    public Barrels( String name, int port) throws RemoteException {
        super();
        this.indiceParaPesquisas= new HashMap<>();
        this.gateway = new Gateway();
        //this.ip = ip;
        this.name=name;
        this.port=port;
    }

    public static void main(String[] args) {
        try{
            String rmiName = "gateway";
            String rmiHost = "localhost";    //fazer com o ficheiro das propriedades ou vars de ambiente
            int rmiPort = 8183;

            Barrels barrel_1 = new Barrels("divo", 1000);

            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            barrel_1.gateway = (GatewayINTER) registry.lookup(rmiName);

            registry.rebind("Barrel", barrel_1);
            System.out.println("O primeiro barrel ta registado no rmi");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
/*
        String words = word.toLowerCase();
        ArrayList<String> urls = new ArrayList<>();
        //ArrayList<String>> sortedURLS = new ArrayList<>();

        for (String query : words.split(" ")) {
            if (indiceParaPesquisas.containsKey(word)) {
                urls.addAll(indiceParaPesquisas.get(query));
            }
            //verificar a "pontuação" para ordenar os resultados que vao aparecer
        }
        return sortedURLS;*/
        return List.of();
    }

    @Override
    public void syncWithReplica() throws RemoteException {
        gateway.syncBarrels();
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