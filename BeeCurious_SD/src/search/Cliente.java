package search;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Cliente implements ClienteINTER {
    
    private GatewayINTER gateway;
    public Cliente() throws NotBoundException, RemoteException {
        Registry registry = LocateRegistry.getRegistry(8183); // Use o mesmo número de porta do Gateway
        this.gateway = (GatewayINTER) registry.lookup("gateway");  // O nome do serviço no RMI Registry
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

    public static void main(String[] args) {
        try{
            Cliente cliente = new Cliente();  // Cria uma instância do Cliente
            cliente.solicitarURL();  // Chama o metodo para o usuário inserir um URL
        } catch (RemoteException| NotBoundException e) {
            e.printStackTrace();
        }
    }
}
