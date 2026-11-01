package clientPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    public static void main(String argv[]) throws Exception {
// Le client crée une socket UDP
        DatagramSocket socket = new DatagramSocket();
        Scanner scanner = new Scanner(System.in);

// L'utilisateur entre son nom
        System.out.print("Entrez votre nom d'utilisateur : ");
        String username = scanner.nextLine();

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

// Prépare un paquet UDP avec le nom d'utilisateur
            String ch = "[" + username + "] : " + input;
            byte buffer[] = ch.getBytes();
            int length = buffer.length;
            DatagramPacket donneesEmises = new DatagramPacket(buffer, length, InetAddress.getByName("localhost"), 1234);
// Le client envoie le paquet au serveur
            socket.send(donneesEmises);
        }
    }
}
