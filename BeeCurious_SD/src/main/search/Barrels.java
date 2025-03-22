package main.search;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Barrels extends UnicastRemoteObject implements BarrelsINTER{
    private HashMap<String, ArrayList<String>> indiceInvertido = new HashMap<>();
    private GatewayINTER gateway;
    private String name;
    private int port;
    private HashMap<String, HashSet<String>> ponteiros= new HashMap<>();
    private static final String ficheiroURLbarrels= "barrels.data";

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
            String rmiHost = "localhost";
            int rmiPort = 8183;

            Barrels barrel1 = criarbarrel("Barrel1",rmiPort);
            Barrels barrel2 = criarbarrel("Barrel2", rmiPort);
            Barrels barrel3 = criarbarrel("Barrel3", rmiPort);

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

    public void ping() throws RemoteException {
    }

    @Override
    public void syncWithReplica() throws RemoteException {
        if (!verificarbarrel(this)) {
            System.out.println("Necessário revivr barrel ");
            try {
                this.reviverBarrel();

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }else{
            gateway.syncBarrels();
        }
    }

    public void reviverBarrel() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("localhost", this.port);
        try {
            registry.unbind(this.name);
            System.out.println("Barrel " + this.name + " removido do RMI Registry.");
            String nome = this.name;
            criarbarrel( nome, this.port);
        }
        catch (Exception e){
            System.out.println (" erro a reviver barrel");
        }

    }

    public void updateIndex(HashMap<String, ArrayList<String>> newIndex) throws RemoteException {
        this.indiceInvertido.clear();
        this.indiceInvertido.putAll(newIndex);
    }

    @Override
    public void indexarURL(String url) throws RemoteException {

    }

    @Override
    public void linksURL(String url, List<String> links) throws RemoteException {

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


    public boolean verificarbarrel(Barrels barrels){
        try{
            this.ping();
            return true;
        }catch(RemoteException e){
            System.out.println("Barrel inativo: "+ this.name);
            return false;
        }
    }


    public static Barrels criarbarrel(String nome, int port){
        try{
            Barrels novo = new Barrels(nome, port);
            Registry registry= LocateRegistry.getRegistry("localhost", novo.port);
            novo.gateway=(GatewayINTER) registry.lookup("gateway");
            novo.gateway.syncBarrels();
            novo.updateIndex(novo.indiceInvertido);
            registry.rebind(nome, novo);
            System.out.println("Barrel criado e sincronizado com sucesso");
            return novo;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private void salvar(){
        synchronized (this) {
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("barrel_index.dat"))) {
                output.writeObject(indiceInvertido);
                System.out.println("Índice salvo com sucesso.");
            } catch (IOException e) {
                System.err.println("Erro ao salvar o índice: " + e.getMessage());
            }
        }
    }

    //nos barels temos de ver quando eles s deligam e dps fazer a conexao de novo com a gateway e ver se a informaçao é a mesma
}

//vao se ligar a gateway atraves d RMI callback
//têm de ter estas funçoes: fazer pesquisa e ...