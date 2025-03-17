// Source code is decompiled from a .class file using FernFlower decompiler.
package search;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Robot extends UnicastRemoteObject implements Cliente, Serializable {
    GatewayINTER index;

   public Robot() throws RemoteException {
   }

   public void executar() {
      try {
         this.index = (GatewayINTER)LocateRegistry.getRegistry(8183).lookup("index");
         this.index.registerClient(this);

         while(true) {
            String url = this.index.takeNext();
            System.out.println(url);
            if (url == null) {
               break;
            }

            Thread.sleep(1000L);
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public static void main(String[] args) {
      try {
        Robot r = new Robot();
         r.executar();
      } catch (RemoteException var3) {
         var3.printStackTrace();
      }

   }

   public void printOnClient() throws RemoteException {
      System.out.println("Print on client");
   }
}
