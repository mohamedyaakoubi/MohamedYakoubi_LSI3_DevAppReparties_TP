package rmiServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rmiService.ConversionImpl;

public class ConversionServer {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            ConversionImpl conversion = new ConversionImpl();
            registry.rebind("conversion", conversion);
            System.out.println("Serveur RMI pret sur le port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
