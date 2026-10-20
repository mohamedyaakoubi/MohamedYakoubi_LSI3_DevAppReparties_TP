package serverPackage;

import commonPackage.Operation2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server2 {

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server waiting for a client to connect on port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("Client connected: " + socket.getInetAddress());

                    ObjectInputStream inObj = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream outObj = new ObjectOutputStream(socket.getOutputStream());

                    while (true) {
                        try {
                            // Receive the Operation2 object from the client
                            Object received = inObj.readObject();
                            if (received instanceof Operation2) {
                                Operation2 op = (Operation2) received;

                                // Compute the result
                                double result = op.compute();

                                // Send the result back as a Double
                                outObj.writeObject(result);
                                System.out.println("Computed: " + op + " = " + result);
                            }
                        } catch (ClassNotFoundException e) {
                            System.err.println("Unknown object type received from client.");
                            break;
                        } catch (ArithmeticException e) {
                            outObj.writeObject("Error: " + e.getMessage());
                            System.out.println("Arithmetic error: " + e.getMessage());
                        } catch (IOException e) {
                            System.out.println("Client disconnected.");
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error with client connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
