package rmiServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rmiService.BanqueImpl;

public class BanqueServer {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            BanqueImpl banque = new BanqueImpl();
            registry.rebind("banque", banque);
            System.out.println("Serveur Banque RMI pret sur le port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
