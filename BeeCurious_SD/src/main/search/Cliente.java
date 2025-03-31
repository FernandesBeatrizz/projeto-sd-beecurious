package main.search;

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
        Registry registry = LocateRegistry.getRegistry("localhost", 8183);
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
                    //aqui meter a dar gateway, barrels e cliente
                    pesquisarconjtermos();
                    break;
                case 3:
                    //gateway, barrels e cliente
                    consultarligacoespagina();
                    break;
                case 4:
                    //apontei q é p usar os downloaders
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
        try {
            String input = sc.nextLine();

            // Divide os termos e remove espaços em branco
            String[] termos = input.toLowerCase().split("\\s+");

            if (termos.length == 0) {
                System.out.println("Nenhum termo de pesquisa fornecido");
                return;
            }

            // Verifica se cada termo existe no índice
            for (String termo : termos) {
                List<String> resultadosParciais = gateway.searchWord(termo);
                if (resultadosParciais.isEmpty()) {
                    System.out.println("Termo '" + termo + "' não encontrado em nenhuma página");
                    return;
                }
            }

            List<String[]> resultados = gateway.top10(input);

            if (resultados.isEmpty()) {
                System.out.println("Nenhum resultado encontrado para todos os termos juntos");
                return;
            }

            // Exibe os resultados formatados
            System.out.println("\nTop " + resultados.size() + " resultados para: " + input);
            for (String[] pagina : resultados) {
                System.out.println("\nTítulo: " + pagina[1]);
                System.out.println("URL: " + pagina[0]);
                System.out.println("Citação: " + pagina[2]);
                System.out.println("Links relacionados: " + pagina[3]);
                System.out.println("---------------------------");
            }

        } catch (RemoteException e) {
            System.err.println("Erro ao pesquisar: " + e.getMessage());
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

