package rmiClient;

import javax.naming.InitialContext;
import metier.Compte;
import rmiService.IBanque;

public class BanqueClient2 {
    public static void main(String[] args) {
        try {
            InitialContext ctx = new InitialContext();
            IBanque banque = (IBanque) ctx.lookup("banque");

            System.out.println(banque.creerCompte(new Compte(1, 1000)));
            System.out.println(banque.creerCompte(new Compte(1, 500)));
            System.out.println(banque.getInfoCompte(1));
            System.out.println(banque.getInfoCompte(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
