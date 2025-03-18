package search;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteINTER extends Remote {
   void printOnClient() throws RemoteException;
}




//forma de comunicaçao entre o servidor e o cliente