package serverPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Server {
    static byte buffer[] = new byte[1024];
    public static void main(String argv[]) throws Exception {
        // Le serveur crée une socket UDP sur le port 1234
        DatagramSocket socket = new DatagramSocket(new InetSocketAddress(1234));
        System.out.println("Lancement du serveur");
        while (true) {
// Le serveur attend un paquet UDP
            DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquet);
            int taille = paquet.getLength();
            String donnees = new String(paquet.getData(), 0, taille);
            System.out.println("[" + paquet.getAddress().getHostAddress() + ":" + paquet.getPort() + "] " + donnees);
        }
    }
}
