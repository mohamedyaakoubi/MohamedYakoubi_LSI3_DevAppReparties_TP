package corbaServer;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import service.BanqueImpl;

public class BanqueServer {

    public static void main(String[] args) {
        try {
            // a) initialiser l'ORB
            ORB orb = ORB.init(args, null);

            // b) récupérer le RootPOA et activer le POAManager
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();

            // c) configurer l'annuaire JNDI (lit jndi.properties)
            Context ctx = new InitialContext();

            // d) créer le servant
            BanqueImpl od = new BanqueImpl();

            // e) enregistrer sa référence dans l'annuaire
            ctx.rebind("BK", poa.servant_to_reference(od));
            System.out.println("Serveur Banque CORBA pret, en attente des clients...");

            // f) attendre les requêtes des clients
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
