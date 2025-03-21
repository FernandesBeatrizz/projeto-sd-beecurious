package main.search;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Barrels extends UnicastRemoteObject implements BarrelsINTER{
    private HashMap<String, ArrayList<String>> indiceInvertido = new HashMap<>();
    private GatewayINTER gateway;
    //private int ip; host??
    private String name;
    private int port;
    private HashMap<String, HashSet<String>> ponteiros= new HashMap<>();

    public Barrels( String name, int port) throws RemoteException {
        super();
        this.indiceInvertido=new HashMap<>();
        this.gateway = new Gateway();
        this.name=name;
        this.port=port;
        this.ponteiros=new HashMap<>();
    }

    public static void main(String[] args) {
        try{
            String rmiName = "gateway";
            String rmiHost = "localhost";    //fazer com o ficheiro das propriedades ou vars de ambiente
            int rmiPort = 8183;

            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);

            Barrels barrel_1 = new Barrels("divo", 1000);
            registry.rebind("Barrel", barrel_1);
            barrel_1.gateway = (GatewayINTER) registry.lookup(rmiName);

            Barrels barrel_2 = new Barrels(rmiName, rmiPort);
            registry.rebind("Barrel2", barrel_2);
            barrel_2.gateway = (GatewayINTER) registry.lookup(rmiName);

            //nao sei s é preciso chamar os 2 barrels com o syncWithReplica
            System.out.println("- - barrels check");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addToIndex(String word, String url) throws RemoteException {
        indiceInvertido.putIfAbsent(word, new ArrayList<>());
        if (!indiceInvertido.get(word).contains(url)) {
            indiceInvertido.get(word).add(url);
        }
    }


    @Override
    public synchronized List<String> searchWord(String word) throws RemoteException {
        String[] words = word.toLowerCase().split(" ");
        ArrayList<String> resultadourls = new ArrayList<>();
        //ArrayList<String>> sortedURLS = new ArrayList<>();

        if (indiceInvertido.containsKey(words[0])) {
            resultadourls.addAll(indiceInvertido.get(words[0])); // Adiciona os URLs que contêm o primeiro termo
        } else {
            return resultadourls; // Se o primeiro termo não existe no índice, retorna lista vazia
        }

        for (int i =1; i<words.length; i++) {
            if (indiceInvertido.containsKey(words[i])) {
                resultadourls.retainAll(indiceInvertido.get(words[i])); //addAll ou retainAll
            }else{
                //resultadourls.clear(); //ver bem este else
                //break;
                return new ArrayList<>();
            }
        }
        return resultadourls;
    }

    @Override
    public void syncWithReplica() throws RemoteException {
        if (!verificarbarrel(this)) {
            System.out.println("Necessário criar outro barrel e sincronizar ");
            criarbarrel(this);
        }else{
            gateway.syncBarrels();
        }
    }

    public void updateIndex(HashMap<String, ArrayList<String>> newIndex) throws RemoteException {
        this.indiceInvertido.clear();
        this.indiceInvertido.putAll(newIndex);
    }

    public LinkedHashMap<String, Integer> top10() throws RemoteException{
        //meter dps um aviso a dizer que vai buscar os 10
        List<Map.Entry<String,ArrayList<String>>> ordenardecrescente = new ArrayList<>(indiceInvertido.entrySet());
        ordenardecrescente.sort((entry1, entry2) -> entry2.getValue().size() - entry1.getValue().size());     //o chat diz q é a soluçao mais simples, o diogo tbm fez assim

        LinkedHashMap<String, Integer> top10 = new LinkedHashMap<>();
        for (int i=0; i<Math.min(10, ordenardecrescente.size()); i++){
            String p=ordenardecrescente.get(i).getKey();
            int count= ordenardecrescente.get(i).getValue().size();
            top10.put(p, count);
        }
        return top10;
    }


    public boolean verificarbarrel(Barrels barrel){
        try{
            barrel.syncWithReplica();
            return true;
        }catch(RemoteException e){
            System.out.println("Barrel não esta so a dormir: "+ barrel.name);
            return false;
        }
    }


    public void criarbarrel(Barrels barrel){
        try{
            Barrels novo = new Barrels(barrel.name, barrel.port);
            Registry registry= LocateRegistry.getRegistry("localhost", barrel.port);
            novo.gateway=(GatewayINTER) registry.lookup("gateway");
            barrel.gateway.syncBarrels();
            novo.updateIndex(barrel.indiceInvertido);
            registry.rebind("Barrel3", novo);
            System.out.println("Barrel criado e sincronizado com sucesso");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    //nos barels temos de ver quando eles s deligam e dps fazer a conexao de novo com a gateway e ver se a informaçao é a mesma
}

//vao se ligar a gateway atraves d RMI callback
//têm de ter estas funçoes: fazer pesquisa e ...