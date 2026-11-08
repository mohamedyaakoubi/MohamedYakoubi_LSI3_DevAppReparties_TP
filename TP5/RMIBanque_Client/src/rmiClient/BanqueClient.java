package rmiClient;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import metier.Compte;
import rmiService.IBanque;

public class BanqueClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 1099;
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            IBanque banque = (IBanque) registry.lookup("banque");

            System.out.println(banque.creerCompte(new Compte(1, 1000)));
            System.out.println(banque.creerCompte(new Compte(1, 500)));
            System.out.println(banque.getInfoCompte(1));
            System.out.println(banque.getInfoCompte(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
