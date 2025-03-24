package main.search;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.util.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;

public class Downloader extends UnicastRemoteObject implements DownloaderINTER, Runnable {

    private static final int timeout = 5000;
    GatewayINTER gateway;
    private static final Set<String> urlsProcessados= new HashSet<>();
    QueueInterface urlQueue;
    private static final String nomesficheiroparaguardar = "informacoescompletas.obj";

    public Downloader(QueueInterface urlQueue) throws RemoteException, InterruptedException {
        super();
        this.urlQueue = urlQueue;
    }

    public void executar() throws RemoteException {
        try {
            String rmiName = "Gateway";
            String rmiHost = "localhost";
            int rmiPort = 8183;

            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            gateway = (GatewayINTER) registry.lookup(rmiName);
            //gateway.registerClient((ClienteINTER) this);  //ver isto tbm

            int tentativas=0;
            while (true) {
                //String url = gateway.takeNext();
                String url= urlQueue.getURL();
                System.out.println(url);
                if(url == null){
                    //break;
                    System.out.println("fila vazia");
                    tentativas+=1;
                    if (tentativas > urlQueue.getMaxSize()) {  // Melhor usar a variável
                        System.out.println("Fila vazia por muito tempo. Encerrando processo.");
                        break;
                    }
                    Thread.sleep(1000);
                    continue;
                }
                tentativas=0;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = transformarUrlAbsoluta("http://" + new URI(url).getHost(), url); // Use uma baseUrl apropriada
                }
                if (!urlsProcessados.contains(url.trim().toLowerCase())) {
                    urlsProcessados.add(url.trim().toLowerCase());
                    processarPagina(url);
                    urlQueue.markURLAsProcessed(url);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /*public void processarPagina(String url) throws RemoteException {
        BarrelsINTER barrel= gateway.getBarrel();
        if (!barrel.containsURL(url)){
            try{
                Document doc= Jsoup.connect(url).get();

                //titulo
                String titulo = doc.title();
                System.out.println(titulo);


                //citação
                String citacao= "";
                Element primeiroparagrafo= doc.select("p").first();
                if (primeiroparagrafo != null) {
                    citacao = primeiroparagrafo.text();
                }else{
                    citacao = doc.select("meta[property=og:description]").attr("content");
                    if (citacao.isEmpty()) {
                        citacao = doc.select("meta[name=description]").attr("content");
                    }
                    if (citacao.isEmpty()) {
                        citacao = "Nenhuma citação encontrada.";
                    }
                }
                System.out.println("Citação: " + citacao);


                //Extrair link
                Elements anchors = doc.select("a");
                String baseUrl = doc.baseUri(); // A URL original do cliente
                List<String> listaLinks = new ArrayList<>();

                if (baseUrl.isEmpty()) {
                    System.err.println("Erro: baseUri() não foi definido corretamente!");
                    return;
                }
                for (Element anchor : anchors) {
                    String href = anchor.attr("href");
                    if (href.isEmpty() || href.startsWith("#")) {
                        continue;
                    }
                    String absoluteUrl=transformarUrlAbsoluta(baseUrl, href);
                    if (absoluteUrl != null) {
                        gateway.putNew(absoluteUrl);
                        System.out.println("Link extraído: " + absoluteUrl);
                    }
                }


                //extrair palavras
                HashMap<String, HashSet<String>> index = new HashMap<>(); // Índice invertido local
                String[] palavras = doc.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
                Set<String> palavrasExtraidas = new HashSet<>();
                for (String palavra : palavras) {
                    if (palavra.length() > 3) { // Evita palavras curtas
                        index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                        gateway.addToIndex(palavra, url);

                        palavrasExtraidas.add(palavra);
                    }
                }

                for (String palavra : palavrasExtraidas) {
                    barrel.addToIndex(palavra, url, titulo, citacao, listaLinks);
                }
            }catch (IOException e){
                System.out.println("Erro"+ e.getMessage());
            }
        }
    }*/
    public void processarPagina(String url) throws RemoteException {
        BarrelsINTER barrel= gateway.getBarrel();
        if (!barrel.containsURL(url)){
            try{
                Document doc= Jsoup.connect(url).get();

                //titulo
                String titulo = doc.title();
                System.out.println(titulo);


                //citação
                String citacao= "";
                Element primeiroparagrafo= doc.select("p").first();
                if (primeiroparagrafo != null) {
                    citacao = primeiroparagrafo.text();
                }else{
                    citacao = doc.select("meta[property=og:description]").attr("content");
                    if (citacao.isEmpty()) {
                        citacao = doc.select("meta[name=description]").attr("content");
                    }
                    if (citacao.isEmpty()) {
                        citacao = "Nenhuma citação encontrada.";
                    }
                }
                System.out.println("Citação: " + citacao);


                //Extrair link
                Elements anchors = doc.select("a");
                String baseUrl = doc.baseUri(); // A URL original do cliente
                List<String> listaLinks = new ArrayList<>();

                if (baseUrl.isEmpty()) {
                    System.err.println("Erro: baseUri() não foi definido corretamente!");
                    return;
                }
                for (Element anchor : anchors) {
                    String href = anchor.attr("href");
                    if (href.isEmpty() || href.startsWith("#")) {
                        continue;
                    }
                    String absoluteUrl=transformarUrlAbsoluta(baseUrl, href);
                    if (absoluteUrl != null) {
                        gateway.putNew(absoluteUrl);
                        System.out.println("Link extraído: " + absoluteUrl);
                    }
                }


                //extrair palavras
                HashMap<String, HashSet<String>> index = new HashMap<>(); // Índice invertido local
                String[] palavras = doc.text().toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
                Set<String> palavrasExtraidas = new HashSet<>();
                for (String palavra : palavras) {
                    index.computeIfAbsent(palavra, k -> new HashSet<>()).add(url);
                    gateway.addToIndex(palavra, url);
                    palavrasExtraidas.add(palavra);
                }

                for(String palavra : palavrasExtraidas){
                    salvarinformacoesnecessarias(palavra, url, titulo, citacao, listaLinks);
                }

                for (String palavra : palavrasExtraidas) {
                    barrel.addToIndex(palavra, url, titulo, citacao, listaLinks);
                }
            }catch (IOException e){
                System.out.println("Erro"+ e.getMessage());
            }
        }
    }



    private String transformarUrlAbsoluta(String baseUrl, String href){
        try {
            URI baseUri = new URI(baseUrl);

            if (href.startsWith("http://") || href.startsWith("https://")) { //se ja for normal ele retorna so
                return href;
            } else if (href.startsWith("/")) { //se começar por isto ele vai construir
                return baseUri.getScheme() + "://" + baseUri.getHost() + href;
            } else {
                String caminhoAtual = baseUrl.substring(0, baseUrl.lastIndexOf("/") + 1);
                return caminhoAtual + href;
            }
        } catch (URISyntaxException e) {
            System.err.println("Erro ao processar URL: " + href);
            return null;
        }
    }



    public void salvarinformacoesnecessarias(String palavra, String url, String titulo, String citacao, List<String>listaLinks){
        try{
            //ObjectOutputStream out;
            File file= new File(nomesficheiroparaguardar);

            //verificar se o arquivo existe
            /*if(file.exists()){
                out = new ObjectOutputStream(new FileOutputStream(file, true)) {
                    @Override
                    protected void writeStreamHeader() throws IOException {
                        // Não sobrescreve o cabeçalho do arquivo
                    }
                };
            }else{
                out= new ObjectOutputStream(new FileOutputStream(file));
            }*/

            List<HashMap<String, Object>> dadosExistentes = new ArrayList<>();

            // Verificar se o arquivo já existe e carregar dados antigos
            if (file.exists() && file.length() > 0) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                    while (true) {
                        try {
                            HashMap<String, Object> dados = (HashMap<String, Object>) in.readObject();
                            dadosExistentes.add(dados);
                        } catch (EOFException e) {
                            break; // Fim do arquivo
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Erro ao ler dados existentes: " + e.getMessage());
                }
            }

            // Verificar se a URL já foi processada
            for (HashMap<String, Object> dados : dadosExistentes) {
                if (dados.get("url").equals(url)) {
                    System.out.println("Informação já processada");
                    return;
                }
            }

            HashMap<String, Object> dados = new HashMap<>();
            dados.put("palavra", palavra);
            dados.put("url", url);
            dados.put("titulo", titulo);
            dados.put("citacao", citacao);
            dados.put("links", listaLinks);

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file, true)) {
                @Override
                protected void writeStreamHeader() throws IOException {
                    if (file.length() == 0) {
                        super.writeStreamHeader(); // Escreve cabeçalho apenas se o arquivo estiver vazio
                    }
                }
            }) {
                out.writeObject(dados);
                System.out.println("As informações foram salvas com sucesso!");
            }

        } catch (IOException e) {
            System.err.println("Erro ao salvar as informações: " + e.getMessage());
        }
    }

@Override
    public void run() {
    try {
        executar();
    } catch (RemoteException e) {
        throw new RuntimeException(e);
    }
}

    public static void main(String[] args) {
        try {
            int numDownloaders=3;
            //URLqueue urlQueue= new URLqueue(100);
            Registry registry = LocateRegistry.getRegistry("localhost", 8183);
            GatewayINTER gateway = (GatewayINTER) registry.lookup("Gateway");
            QueueInterface urlQueue = gateway.getUrlQueue();

            Thread[] threads = new Thread[numDownloaders];
            for (int i = 0; i < numDownloaders; i++) {
                Downloader d= new Downloader(urlQueue);
                threads[i]= new Thread(d);
                threads[i].start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

    }

    public String get_url() throws RemoteException {
        return gateway.get_url();
    }

    public void put_url(String url) throws RemoteException {
        gateway.putNew(url);
    }

    public void save_words(String word, String url) throws java.rmi.RemoteException {
        gateway.addToIndex(word, url);
    }

}
