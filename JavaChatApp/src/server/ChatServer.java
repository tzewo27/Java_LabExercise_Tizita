// package server;

// import java.io.*;
// import java.net.*;

// public class ChatServer {
//     public static void main(String[] args) {
//         int port = 5000;

//         try (ServerSocket serverSocket = new ServerSocket(port)) {
//             System.out.println("Chat Server started on port " + port);
//             System.out.println("Waiting for clients...");

//             // This loop runs FOREVER
//             // Each time a client connects, it creates a new Thread
//             while (true) {
//                 // Wait for a new client to connect
//                 Socket socket = serverSocket.accept();

//                 // Create a "Waiter" (Handler) for this specific client
//                 ClientHandler clientHandler = new ClientHandler(socket);

//                 // Start a new Thread so this client runs independently
//                 Thread thread = new Thread(clientHandler);
//                 thread.start();

//                 System.out.println("New thread started for a client.");
//             }
//         } catch (IOException e) {
//             System.out.println("Server error: " + e.getMessage());
//         }
//     }}



package server;
import java.net.*;
import java.io.*;

public class ChatServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server started on port 5000...");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}



