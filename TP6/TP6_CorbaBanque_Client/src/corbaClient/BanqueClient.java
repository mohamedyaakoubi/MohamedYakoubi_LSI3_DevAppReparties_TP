package corbaClient;

import javax.naming.Context;
import javax.naming.InitialContext;

import corbaBanque.Compte;
import corbaBanque.IBanqueRemote;
import corbaBanque.IBanqueRemoteHelper;

public class BanqueClient {

    public static void main(String[] args) {
        try {
            // a) récupérer les propriétés de l'annuaire JNDI
            Context ctx = new InitialContext();

            // b) récupérer la référence de l'objet distant
            Object ref = ctx.lookup("BK");

            // c) convertir la référence en IBanqueRemote (création du stub)
            IBanqueRemote stub = IBanqueRemoteHelper.narrow((org.omg.CORBA.Object) ref);

            // d) appeler les méthodes distantes
            stub.creerCompte(new Compte(1, 1000));
            stub.creerCompte(new Compte(2, 500));
            System.out.println("Comptes 1 et 2 crees");

            stub.verser(300, 1);
            System.out.println("Versement de 300 sur le compte 1");

            stub.retirer(200, 2);
            System.out.println("Retrait de 200 du compte 2");

            Compte c = stub.getCompte(1);
            System.out.println("Compte " + c.code + " : solde = " + c.solde);

            System.out.println("Liste des comptes :");
            for (Compte cpte : stub.getComptes()) {
                System.out.println("  Compte " + cpte.code + " : solde = " + cpte.solde);
            }

            System.out.println("100 euros = " + stub.conversion(100) + " DT");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
