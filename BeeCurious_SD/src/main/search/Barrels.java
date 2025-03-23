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
    private HashMap<String, ArrayList<String[]>> indiceInvertido = new HashMap<>();
    private GatewayINTER gateway;
    private String name;
    private int port;
    private HashMap<String, HashSet<String>> ponteiros= new HashMap<>();
    private static final String ficheiroURLbarrels= "barrels.data";

    public Barrels( String name, int port) throws RemoteException {
        super(port);
        this.indiceInvertido=new HashMap<>();
        this.name = name;
        this.port = port;
        this.ponteiros = new HashMap<>();

    }

    public static void main(String[] args) {
        try{
            String gatewayName = "gateway";
            String gatewayHost = "localhost";
            int gatewayPort = 8183;

            Barrels barrel1 = criarbarrel("Barrel1", 1000, gatewayPort );
            Barrels barrel2 = criarbarrel("Barrel2", 2000, gatewayPort );
            Barrels barrel3 = criarbarrel("Barrel3", 3000, gatewayPort);

            System.out.println("- - barrels check");

            //p ver s a FUNCIONALIDADE 3 esta a dar
            /*barrel1.addToIndex("borba", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade em Portugal conhecida pelo seu mármore.", new ArrayList<>());
            barrel1.addToIndex("mármore", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade em Portugal conhecida pelo seu mármore.", new ArrayList<>());
            barrel1.addToIndex("portugal", "https://exemplo.com/borba", "Cidade de Borba", "Borba é uma cidade em Portugal conhecida pelo seu mármore.", new ArrayList<>());
*/
            for (int i = 1; i <= 15; i++) {
                String termo = "borba";
                String url = "https://exemplo.com/borba" + i;  // URL única para cada entrada
                String titulo = "Cidade de Borba " + i;
                String citacao = "Borba é uma cidade em Portugal conhecida pelo seu mármore. Entrada " + i;
                barrel1.addToIndex(termo, url, titulo, citacao, new ArrayList<>());
            }

            //estas 2 de baixo era p testar a funcionalidade 3. ela esta a dar mas o output p borba é palavra nao encontrada
            String termos="borba";
            barrel1.top10(termos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addToIndex(String word, String url, String titulo, String citacao, List<String>links) throws RemoteException {
        indiceInvertido.putIfAbsent(word, new ArrayList<>());
        /*boolean urlexiste= false;

        if (!indiceInvertido.get(word).contains(url)) {
            indiceInvertido.get(word).add(url);
        }*/
        boolean urlExiste = false;
        for (String[] pagina : indiceInvertido.get(word)) {
            if (pagina[0].equals(url)) {
                urlExiste = true;
                break;
            }
        }

        if (!urlExiste) {
            // Armazena a URL, título, citação e links como um array
            indiceInvertido.get(word).add(new String[]{url, titulo, citacao, String.join(",", links)});
        }
        salvar();
    }


    @Override
    public synchronized List<String> searchWord(String word) throws RemoteException {
        String[] words = word.toLowerCase().split(" ");

        if (words.length == 0)
            return new ArrayList<>();

        ArrayList<String> resultadourls = new ArrayList<>();
        //ArrayList<String>> sortedURLS = new ArrayList<>();

        if (indiceInvertido.containsKey(words[0])) {
            for(String[] pagina : indiceInvertido.get(words[0])) {
                resultadourls.add(pagina[0]);
            }
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
            System.out.println("Necessário reviver barrel ");
            try {
                this.reviverBarrel();

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }else{
            gateway.syncBarrels();
        }
    }

    @Override
    public void updateIndex(HashMap<String, ArrayList<String>> indiceParaPesquisas) throws RemoteException {
        
    }

    public void reviverBarrel() throws RemoteException {
        Registry registry = LocateRegistry.getRegistry("localhost", this.port);
        try {
            registry.unbind(this.name);
            this.gateway.unregisterBarrel(this);
            System.out.println("Barrel " + this.name + " removido do RMI Registry.");
            String nome = this.name;
            criarbarrel( nome, this.port, 8183);
        }
        catch (Exception e){
            System.out.println (" erro a reviver barrel");
        }

    }
/*
    public void updateIndex(HashMap<String, ArrayList<String[]>> newIndex) throws RemoteException {
        this.indiceInvertido.clear();
        this.indiceInvertido.putAll(newIndex);
    }*/

    @Override
    public void indexarURL(String url) throws RemoteException {

    }

    /*public void linksURL(String url, List<String> links) throws RemoteException {

    }*/


    //ver estas 2 funçoes melhor!!!
    private int pagina=1;
    public List<String[]> top10(String termos) throws RemoteException{
        String[] palavras = termos.toLowerCase().split(" ");
        List<String[]> resultados = new ArrayList<>();

        //vou ver s os termos existem no indiceinvertido
        for(String palavra : palavras) {
            if(!indiceInvertido.containsKey(palavra)){
                System.out.println("palavra não encontrada");
                return resultados; //s nao encontrar nd retorna lista vazia
            }
        }
        resultados.addAll(indiceInvertido.get(palavras[0])); //todas as paginas q contêm o primeiro termo

        // Filtra os resultados para páginas que contêm todos os termos
        for (int i = 1; i < palavras.length; i++) {
            List<String[]> tempResultados = new ArrayList<>();
            Set<String> urlsExistentes = new HashSet<>();

            // Armazena as URLs atuais
            for (String[] pagina : resultados) {
                urlsExistentes.add(pagina[0]);
            }

            // Adiciona páginas que contenham o termo atual e estejam nas URLs anteriores
            for (String[] pagina : indiceInvertido.get(palavras[i])) {
                if (urlsExistentes.contains(pagina[0])) {
                    tempResultados.add(pagina);
                }
            }
            resultados = tempResultados;
        }

/*isto era oq tinha antes-----------------------------------------
        String maisrestrito= palavras[0]; //pq é oq aparece em menos paginas p restringir os resultados
        int menortamanho= indiceInvertido.get(maisrestrito).size();
        for(String palavra : palavras) {
            if(!palavra.equals(maisrestrito)){
                resultados.retainAll(indiceInvertido.get(palavra));
            }
        }*/

        //ordena pela quantidade d termos
        /*resultados.sort((pagina1, pagina2) -> {
            int count1 = contarTermosNaPagina(pagina1, palavras);
            int count2 = contarTermosNaPagina(pagina2, palavras);
            return Integer.compare(count2, count1); // Ordena em ordem decrescente
        });*/

        //ordeno os resultados pela quantidade de termos encontrados
        resultados.sort(new Comparator<String[]>() {
            public int compare(String[] pag1, String[] pag2) {
                int count1= contarTermosNaPagina(pag1, palavras);
                int count2= contarTermosNaPagina(pag2, palavras);
                return Integer.compare(count2, count1);
            }
        });

        int resultadospag=10;
        /*
        int inicio=(pagina-1)*resultadospag;
        int fim=Math.min(inicio + resultadospag, resultados.size());

        List<String[]> topResultados = resultados.subList(inicio,fim);//aqui como pode existir menos d 10, temos d ver qual é o mais pequeno

        if (topResultados.isEmpty()) {
            System.out.println("Nenhum resultado encontrado para: " + termos);
        } else {
            System.out.println("\nResultados da pesquisa para: " + termos+ " -página " + pagina);
            for (String[] resultado : topResultados) {
                System.out.println("Título: " + resultado[1]);
                System.out.println("URL: " + resultado[0]);
                System.out.println("Citação: " + resultado[2]);
                System.out.println("---------------------------");
            }
        }
        pagina++;*/
        int totalPaginas = (int) Math.ceil((double) resultados.size() / resultadospag);

        if (pagina <= totalPaginas) {
            int inicio = (pagina - 1) * resultadospag; // Página começa a contar de 1
            int fim = Math.min(inicio + resultadospag, resultados.size());

            // Exibe os resultados da página atual
            List<String[]> resultadosDaPagina = resultados.subList(inicio, fim);

            System.out.println("\nResultados da pesquisa para: " + termos + " - Página " + pagina);
            for (String[] resultado : resultadosDaPagina) {
                System.out.println("Título: " + resultado[1]);
                System.out.println("URL: " + resultado[0]);
                System.out.println("Citação: " + resultado[2]);
                System.out.println("---------------------------");
            }

            // Aumenta a página para a próxima chamada
            pagina++;
        } else {
            System.out.println("Fim dos resultados.");
        }

        return resultados;
    }


    private int contarTermosNaPagina(String[] pagina, String[] termos) {
        int count = 0;
        String conteudo = pagina[1] + " " + pagina[2]; // Título + citação

        for (String termo : termos) {
            if (conteudo.toLowerCase().contains(termo)) {
                count++;
            }
        }

        return count;
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


    public static Barrels criarbarrel(String barrel_nome, int barrel_port, int gateway_port){
        try{
            Barrels novo = new Barrels(barrel_nome, barrel_port);
            Registry registry = LocateRegistry.getRegistry("localhost", gateway_port);
            registry.rebind(barrel_nome, novo);

            novo.gateway = (GatewayINTER) registry.lookup("Gateway");
            novo.gateway.registerBarrel(novo);

            return novo;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

//era o metodo de guardar as coisas que estao na queue, ainda nao acabei
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



    public boolean containsURL(String url){
        for (ArrayList<String[]> paginas : indiceInvertido.values()) {
            for(String[] pagina: paginas){
                if (pagina[0].equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }


    //nos barels temos de ver quando eles s deligam e dps fazer a conexao de novo com a gateway e ver se a informaçao é a mesma
}

//vao se ligar a gateway atraves d RMI callback
//têm de ter estas funçoes: fazer pesquisa e ...