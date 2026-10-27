package serverPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMultiThread2 {

    private static final int PORT = 12345;

    // compteur global partagé entre tous les threads ClientProcess2
    private static int nbOperations = 0;

    // synchronized : un seul thread à la fois peut modifier le compteur
    public static synchronized int incrementerCompteur() {
        nbOperations++;
        return nbOperations;
    }

    public static void main(String[] args) {
        int numero = 0; // numéro d'ordre de connexion

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Serveur calculatrice multi-thread en attente sur le port " + PORT + "…");

            while (true) {
                Socket socket = serverSocket.accept();
                numero++;
                System.out.println("Client n°" + numero + " connecté, adresse : "
                        + socket.getRemoteSocketAddress());

                // un thread dédié pour chaque client
                new ClientProcess2(socket, numero).start();
            }
        } catch (IOException e) {
            System.out.println("Erreur côté serveur : " + e.getMessage());
        }
    }
}
