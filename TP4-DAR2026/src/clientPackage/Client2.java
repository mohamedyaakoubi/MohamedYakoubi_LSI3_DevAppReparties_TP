package clientPackage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client2 {
    static DatagramSocket socket;

    public static void main(String argv[]) throws Exception {

        // =====================================
        // 1. Création de la socket UDP
        // =====================================

        socket = new DatagramSocket();

        // =====================================
        // 2. Nom d'utilisateur
        // =====================================

        Scanner scanner = new Scanner(System.in);

        System.out.print("Entrez votre nom d'utilisateur : ");
        String nomUtilisateur = scanner.nextLine();

        // =====================================
        // 3. Adresse du serveur
        // =====================================

        int portServeur = 1234;

        InetSocketAddress adresseServeur =
                new InetSocketAddress("localhost", portServeur);

        // =====================================
        // 4. Thread de réception
        // =====================================

        Thread threadReception = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {

                    try {

                        // Tableau d'octets pour recevoir
                        byte[] bufferReception =
                                new byte[1024];


                        // Création du DatagramPacket
                        DatagramPacket packetReception =
                                new DatagramPacket(
                                        bufferReception,
                                        bufferReception.length
                                );


                        // Attente d'un message du serveur
                        socket.receive(packetReception);


                        // Conversion byte[] -> String
                        String message =
                                new String(
                                        packetReception.getData(),
                                        0,
                                        packetReception.getLength()
                                );


                        // Affichage du message
                        System.out.println();

                        System.out.println(
                                "[Reçu] " + message
                        );

                        System.out.print(
                                nomUtilisateur + " : "
                        );


                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
        });


        // =====================================
        // 5. Configuration et démarrage du thread
        // =====================================

        threadReception.setDaemon(true);

        threadReception.start();


        // =====================================
        // 6. Thread principal : ENVOI
        // =====================================

        while (true) {

            System.out.print(
                    nomUtilisateur + " : "
            );


            // Lecture du message
            String message =
                    scanner.nextLine();


            // Ajouter le nom utilisateur
            String messageComplet =
                    "[" + nomUtilisateur + "] : "
                            + message;


            // Conversion String -> byte[]
            byte[] bufferEnvoi =
                    messageComplet.getBytes();


            // Création du DatagramPacket
            DatagramPacket packetEnvoi =
                    new DatagramPacket(
                            bufferEnvoi,
                            bufferEnvoi.length,
                            adresseServeur
                    );


            // Envoi du message
            socket.send(packetEnvoi);
        }

    }
}
