package main.search;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClienteINTER extends Remote {
   void printOnClient() throws RemoteException;

   public List<String> searchWord(String word) throws RemoteException;
}




//forma de comunicaçao entre o servidor e o cliente