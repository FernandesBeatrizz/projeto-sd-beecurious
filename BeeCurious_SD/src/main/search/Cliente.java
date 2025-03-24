package main.search;

import java.rmi.NotBoundException;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Cliente implements ClienteINTER {

    private GatewayINTER gateway;
    private Scanner sc;

    public Cliente() throws NotBoundException, RemoteException {
        Registry registry = LocateRegistry.getRegistry("localhost", 8183);
        this.gateway = (GatewayINTER) registry.lookup("Gateway");
        this.sc = new Scanner(System.in);
    }

    public static void main(String[] args) {
        try {
            Cliente cliente = new Cliente();  // Cria uma instância do Cliente
            cliente.exibirMenu();


            cliente.solicitarURL();
            ;// Chama o metodo para o usuário inserir um URL
            cliente.run();


        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void exibirMenu() throws RemoteException {
        int opcao = 0;
        boolean exit = false;
        while (!exit) {
            System.out.println("\nEscolha uma opção");
            System.out.println("1 - indexar URLs");
            System.out.println("2 - Pesquisar paginas");
            System.out.println("3 - Ordenar resultados por importância");
            System.out.println("4 - Consultar ligações para uma página");
            System.out.println("5 - Aprendizagem de palavras vazias");
            System.out.println("6- Sair");
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
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    exit = true;
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }


    @Override
    public void run() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        String word = sc.nextLine();
        List<String> resultados = this.searchWord(word);
        System.out.print(resultados);
    }

    @Override
    public void printOnClient() throws RemoteException {
    }


    public void solicitarURL() throws RemoteException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Digite um URL: ");
        String url = sc.nextLine();
        gateway.putNew(url);
        System.out.println("URL enviado: " + url);
    }

    @Override
    public synchronized List<String> searchWord(String word) throws RemoteException {
        try {
            return gateway.searchWord(word);
        } catch (Exception e) {
            System.out.println("falha na pesquisa");
            return new ArrayList<>();
        }
    }

    public void pesquisarconjtermos() throws RemoteException {
        try {
            System.out.println("Digite os termos de pesquisa: ");
            String termos = sc.nextLine();

            List<String[]> resultados = gateway.top10(termos);
            if (resultados.isEmpty()) {
                System.out.println("Nenhum resultado encontrado");
                return;
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao pesquisar: " + e.getMessage());
        }
    }
}

