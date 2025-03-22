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

    public static void main(String[] args) {
        try{
            Cliente cliente = new Cliente();  // Cria uma instância do Cliente
            cliente.solicitarURL();  // Chama o metodo para o usuário inserir um URL
        } catch (RemoteException| NotBoundException e) {
            e.printStackTrace();
        }
    }
}
