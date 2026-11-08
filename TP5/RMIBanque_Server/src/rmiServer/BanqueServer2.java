package rmiServer;

import java.rmi.registry.LocateRegistry;
import javax.naming.InitialContext;
import rmiService.BanqueImpl;

public class BanqueServer2 {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
        try {
            LocateRegistry.createRegistry(port);
            BanqueImpl banque = new BanqueImpl();
            InitialContext ctx = new InitialContext();
            ctx.rebind("banque", banque);
            System.out.println("Serveur Banque RMI + JNDI pret sur le port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
