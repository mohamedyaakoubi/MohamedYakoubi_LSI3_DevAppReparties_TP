package serverPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Server2 {
    static byte buffer[] = new byte[1024];
    // Liste des clients connectes
    static List<SocketAddress> clients = new ArrayList<>();

    public static void main(String argv[]) throws Exception {
        DatagramSocket socket = new DatagramSocket(new InetSocketAddress(1234));
        System.out.println("Lancement du serveur multi-clients");
        while (true) {
// Le serveur attend un paquet UDP
            DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquet);
            int taille = paquet.getLength();
            String donnees = new String(paquet.getData(), 0, taille);
            SocketAddress expéditeur = paquet.getSocketAddress();

// Si le client n'est pas dans la liste, on l'ajoute
            if (!clients.contains(expéditeur)) {
                clients.add(expéditeur);
                System.out.println("Nouveau client connecté : " + expéditeur);
            }

            System.out.println("[" + paquet.getAddress().getHostAddress() + ":" + paquet.getPort() + "] " + donnees);

// On diffuse le paquet à tous les autres clients
            for (SocketAddress client : clients) {
                if (!client.equals(expéditeur)) {
                    DatagramPacket paquetEnvoyé = new DatagramPacket(
                        paquet.getData(), paquet.getLength(), client);
                    socket.send(paquetEnvoyé);
                }
            }
        }
    }
}
