package search;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface cliente extends Remote {
   void printOnClient() throws RemoteException;
}