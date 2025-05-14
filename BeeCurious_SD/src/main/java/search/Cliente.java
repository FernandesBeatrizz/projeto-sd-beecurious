package main.java.search;

import java.rmi.NotBoundException;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
/**
 * Classe que implementa a interface do cliente para o motor de pesuisa.
 * <p>
 * Esta classe permite aos clientes interagir com o sistema através de um menu, fornecendo as funcionalidades propostas no enunciado</p>
 *
 */
public class Cliente implements ClienteINTER {

    private final GatewayINTER gateway;
    private final Scanner sc;
    /**
     * Construtor da classe Cliente.
     *
     * <p>Estabelece a conexão com o servidor RMI e obtém a referência para o Gateway.</p>
     */
    public Cliente() throws NotBoundException, RemoteException {
        Registry registry = LocateRegistry.getRegistry("localHost", 8183);
        this.gateway = (GatewayINTER) registry.lookup("Gateway");
        this.sc = new Scanner(System.in);
    }


    public static void main(String[] args) {
        try {
            Cliente cliente = new Cliente();
            cliente.exibirMenu();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exibe o menu principal e processa as opções do user.
     *
     */
    public void exibirMenu() throws RemoteException {
        int opcao = 0;
        boolean exit = false;
        while (!exit) {
            System.out.println("\nEscolha uma opção");
            System.out.println("1 - indexar URLs"); //funcionalidade 1 e 2
            System.out.println("2 - Pesquisar paginas por importância"); //funcionalidade 3 e 4
            System.out.println("3 - Consultar ligações para uma página"); //funcionalidade 5
            System.out.println("4 - Aprendizagem de palavras vazias"); //funcionalidade 6
            System.out.println("5- Sair");
            System.out.print("Qual opção quer: ");

            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    solicitarURL();
                    break;
                case 2:
                    pesquisarconjtermos();
                    break;
                case 3:
                    consultarligacoespagina();
                    break;
                case 4:
                    Set<String> stopWords = gateway.getBarrel().getStopWords();
                    System.out.println(stopWords);
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    public void solicitarURL() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite um URL: ");
        String url = sc.nextLine();
        gateway.putNew(url);
        System.out.println("URL enviado: " + url);
    }

    public void pesquisarconjtermos() throws RemoteException {
        System.out.println("Digite o(s) termo(s) de pesquisa:");
        String input = sc.nextLine().trim();
        // Obtém os resultados do Barrel via Gateway
        List<String[]> resultados = gateway.searchWord(input);
        // Imprime os resultados formatados
        if (resultados.isEmpty()) {
            System.out.println("Nenhum resultado encontrado para: '" + input + "'");
        }
        int pagina = 1;
        int resultadospag = 10;
        int totalPaginas = (int) Math.ceil((double) resultados.size() / resultadospag);
        boolean sair = false;

        while (pagina <= totalPaginas && !sair) {
            int inicio = (pagina - 1) * resultadospag; // Página começa a contar de 1
            int fim = Math.min(inicio + resultadospag, resultados.size());
            // Exibe os resultados da página atual
            List<String[]> resultadosDaPagina = resultados.subList(inicio, fim);
            System.out.println("\nResultados da pesquisa para: " + input + " - Página " + pagina);
            for (String[] resultado : resultadosDaPagina) {
                System.out.println("Título: " + resultado[1]);
                System.out.println("URL: " + resultado[0]);
                System.out.println("Citação: " + resultado[2]);
                System.out.println("---------------------------");
            }
            // Aumenta a página para a próxima chamada
            //pagina++;
            if (pagina <= totalPaginas) {

                System.out.println("Pressione Enter para ver a próxima página ou digite 'sair' para voltar ao menu...");
                String entrada = sc.nextLine();
                if (entrada.equalsIgnoreCase("sair")) {
                    sair = true;
                } else {
                    pagina++;
                }
            } else {
                System.out.println("Fim dos resultados. Pressione Enter para voltar ao menu.");
                sc.nextLine();
                sair = true;
            }
        }
    }

    public void consultarligacoespagina() throws RemoteException {
        System.out.println("Digite a URL para consultar ligações: ");
        String url = sc.nextLine();
        List<String> paginas = gateway.obterPaginasApontamPara(url);

        if (paginas.isEmpty()) {
            System.out.println("Nenhuma página encontrada que aponte para esta URL.");
        } else {
            System.out.println("As seguintes páginas apontam para " + url + ":");
            for (String pagina : paginas) {
                System.out.println(pagina);
            }
        }
    }
}

