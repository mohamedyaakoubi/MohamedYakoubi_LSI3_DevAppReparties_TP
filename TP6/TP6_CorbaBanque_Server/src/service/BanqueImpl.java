package service;

import java.util.HashMap;
import java.util.Map;

import corbaBanque.Compte;
import corbaBanque.IBanqueRemotePOA;

public class BanqueImpl extends IBanqueRemotePOA {

    // tous les comptes du serveur, rangés par code
    private Map<Integer, Compte> comptes = new HashMap<Integer, Compte>();

    @Override
    public void creerCompte(Compte cpte) {
        comptes.put(cpte.code, cpte);
        System.out.println("Compte " + cpte.code + " cree avec un solde de " + cpte.solde);
    }

    @Override
    public void verser(float mt, int code) {
        Compte cpte = comptes.get(code);
        cpte.solde = cpte.solde + mt;
        System.out.println("Versement de " + mt + " sur le compte " + code + ", nouveau solde : " + cpte.solde);
    }

    @Override
    public void retirer(float mt, int code) {
        Compte cpte = comptes.get(code);
        if (cpte.solde < mt) {
            System.out.println("Retrait de " + mt + " refuse sur le compte " + code + " : solde insuffisant");
            return;
        }
        cpte.solde = cpte.solde - mt;
        System.out.println("Retrait de " + mt + " sur le compte " + code + ", nouveau solde : " + cpte.solde);
    }

    @Override
    public Compte getCompte(int code) {
        System.out.println("Consultation du compte " + code);
        return comptes.get(code);
    }

    @Override
    public Compte[] getComptes() {
        System.out.println("Consultation de la liste des comptes");
        return comptes.values().toArray(new Compte[0]);
    }

    @Override
    public double conversion(float mt) {
        System.out.println("Conversion de " + mt + " euros");
        return mt * 3.3;
    }
}
