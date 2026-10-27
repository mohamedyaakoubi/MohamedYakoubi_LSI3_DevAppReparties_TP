package serverPackage;

import commonPackage.Operation2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientProcess2 extends Thread {

    private final Socket socket;
    private final int clientNumber;

    public ClientProcess2(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outObj = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inObj = new ObjectInputStream(socket.getInputStream());

            // envoyer au client son numéro d'ordre
            outObj.writeObject("Bienvenue, vous êtes le client n°" + clientNumber);

            while (true) {
                // recevoir l'objet Operation2
                Operation2 op = (Operation2) inObj.readObject();
                try {
                    double resultat = op.compute();
                    outObj.writeObject(resultat);
                    int total = ServerMultiThread2.incrementerCompteur();
                    System.out.println("Client n°" + clientNumber + " : " + op + " = " + resultat
                            + " | total des opérations traitées : " + total);
                } catch (ArithmeticException e) {
                    outObj.writeObject("Erreur : " + e.getMessage());
                    System.out.println("Client n°" + clientNumber + " : " + op + " -> erreur " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Client n°" + clientNumber + " déconnecté");
        } catch (ClassNotFoundException e) {
            System.out.println("Objet inconnu reçu du client n°" + clientNumber);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Erreur de fermeture du socket client n°" + clientNumber);
            }
        }
    }
}
