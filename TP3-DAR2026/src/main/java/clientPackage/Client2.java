package clientPackage;

import commonPackage.Operation2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client2 {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        // Allow overriding host via command line argument for LAN testing
        if (args.length > 0) {
            host = args[0];
        }

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to server on " + host + ":" + port);

            ObjectOutputStream outObj = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inObj = new ObjectInputStream(socket.getInputStream());
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Read the client number sent by the server
            try {
                Object clientNumber = inObj.readObject();
                if (clientNumber instanceof String) {
                    System.out.println("Server says: " + clientNumber);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown object type received from server.");
            }

            System.out.println("Enter an operation (e.g. 55 * 25) or 'exit' to quit:");

            while (true) {
                System.out.print(">> ");
                String input = console.readLine();

                if (input == null || input.trim().equalsIgnoreCase("exit")) {
                    break;
                }

                // Validate input on the client side before sending
                try {
                    Operation2 op = Operation2.parse(input);
                    // Send the Operation2 object to the server
                    outObj.writeObject(op);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input: " + e.getMessage());
                    System.out.println("Format: <number> <operator> <number>  (operators: +, -, *, /)");
                    continue;
                }

                // Read the result object from the server
                try {
                    Object response = inObj.readObject();
                    if (response instanceof Double) {
                        System.out.println("Result: " + response);
                    } else if (response instanceof String) {
                        System.out.println("Result: " + response);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown object type received from server.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}
