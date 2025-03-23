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
    public Cliente() throws NotBoundException, RemoteException {
        Registry registry = LocateRegistry.getRegistry("localhost", 8183);
        this.gateway = (GatewayINTER) registry.lookup("Gateway");
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
    public synchronized List<String> searchWord(String word) throws RemoteException{
        try {
            List<String> resultados = gateway.searchWord(word);
            return resultados;
        } catch (Exception e) {
            System.out.println("falha na pesquisa");
            return new ArrayList<>();
        }
    }

    public void pesquisarconjtermos(Scanner sc) throws RemoteException {
        try{
            System.out.println("Digite os termos de pesquisa: ");
            String termos = sc.nextLine();

            List<String[]>resultados=gateway.getBarrel().top10(termos);
            if (resultados.isEmpty()){
                System.out.println("Nenhum resultado encontrado");
                return;
            }

            int paginaAtual = 1;
            boolean sair = false;

            while (!sair) {
                System.out.println("\n=== Página " + paginaAtual + " ===");

                // Exibir 10 resultados por página
                int inicio = (paginaAtual - 1) * 10;
                int fim = Math.min(inicio + 10, resultados.size());

                for (int i = inicio; i < fim; i++) {
                    String[] pagina = resultados.get(i);
                    String titulo = pagina[1];
                    String url = pagina[0];
                    String citacao = pagina[2];

                    System.out.println("Título: " + titulo);
                    System.out.println("URL: " + url);
                    System.out.println("Citação: " + citacao);
                    System.out.println("-----------------------------");
                }

                // Opções de navegação
                System.out.println("\nEscolha uma opção:");
                System.out.println("1 - Próxima página");
                System.out.println("2 - Página anterior");
                System.out.println("3 - Voltar ao menu");
                System.out.print("Opção: ");
                int opcaoPagina = sc.nextInt();
                sc.nextLine(); // Consumir a nova linha

                switch (opcaoPagina) {
                    case 1:
                        if (fim < resultados.size()) {
                            paginaAtual++;
                        } else {
                            System.out.println("Você já está na última página.");
                        }
                        break;
                    case 2:
                        if (paginaAtual > 1) {
                            paginaAtual--;
                        } else {
                            System.out.println("Você já está na primeira página.");
                        }
                        break;
                    case 3:
                        sair = true;
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            }
        } catch (RemoteException e) {
            System.err.println("Erro ao pesquisar: " + e.getMessage());
        }
        }
    }

    public static void main(String[] args) {
        try{
            Cliente cliente = new Cliente();  // Cria uma instância do Cliente
            cliente.solicitarURL();  // Chama o metodo para o usuário inserir um URL
        } catch (RemoteException| NotBoundException e) {
            e.printStackTrace();
        }
    }
}
