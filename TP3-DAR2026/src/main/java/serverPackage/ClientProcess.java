package serverPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientProcess extends Thread {

    private final Socket socket;
    private final int clientNumber;

    public ClientProcess(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        String clientAddress = socket.getRemoteSocketAddress().toString();
        System.out.println("Client n°" + clientNumber + " connecté depuis " + clientAddress);

        try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("Bienvenue, vous êtes le client n°" + clientNumber);

            String line;
            while ((line = reader.readLine()) != null && !line.equalsIgnoreCase("quit")) {
                System.out.println("Client n°" + clientNumber + " a envoyé : " + line);
                writer.println("Echo : " + line);
            }

            System.out.println("Client n°" + clientNumber + " déconnecté");
        } catch (IOException e) {
            System.out.println("Erreur client n°" + clientNumber + " : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Erreur de fermeture du socket client n°"
                        + clientNumber + " : " + e.getMessage());
            }
        }
    }
}


