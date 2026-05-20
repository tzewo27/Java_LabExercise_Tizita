// package server;

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class ClientHandler implements Runnable {
    
//     // This list is "shared" between ALL threads
//     // It holds every connected client so we can send messages to all of them
//     // "static" means it belongs to the CLASS, not just one object
//     public static List<ClientHandler> allClients = new ArrayList<>();
    
//     private Socket socket;
//     private BufferedReader reader;
//     private PrintWriter writer;
//     private String clientName;

//     // Constructor: Called when a new client connects
//     public ClientHandler(Socket socket) {
//         this.socket = socket;
//         try {
//             // Setup reading and writing streams for THIS client
//             InputStream input = socket.getInputStream();
//             reader = new BufferedReader(new InputStreamReader(input));

//             OutputStream output = socket.getOutputStream();
//             // "true" means auto-flush (send instantly)
//             writer = new PrintWriter(output, true);

//             // Add this client to the shared list
//             allClients.add(this);

//             // First message from client will be their name
//             clientName = reader.readLine();
//             System.out.println(clientName + " has joined the chat!");

//             // Tell everyone someone joined
//             broadcastMessage("SERVER", clientName + " has joined the chat!");

//         } catch (IOException e) {
//             System.out.println("Error setting up client: " + e.getMessage());
//         }
//     }

//     // run() is called automatically when the Thread starts
//     @Override
//     public void run() {
//         String message;
//         try {
//             // Keep listening for messages FOREVER (until client disconnects)
//             while ((message = reader.readLine()) != null) {
//                 System.out.println(clientName + ": " + message);

//                 // Save to database (Step 1!)
//                 DatabaseManager.saveMessage(clientName, message);

//                 // Send to ALL other clients
//                 broadcastMessage(clientName, message);
//             }
//         } catch (IOException e) {
//             System.out.println(clientName + " has disconnected.");
//         } finally {
//             // Remove this client from the list when they leave
//             allClients.remove(this);
//             broadcastMessage("SERVER", clientName + " has left the chat.");
//             try {
//                 socket.close();
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     // This method sends a message to EVERY connected client
//     private void broadcastMessage(String sender, String message) {
//         for (ClientHandler client : allClients) {
//             // Don't send the message back to the person who sent it
//             if (!client.clientName.equals(this.clientName)) {
//                 client.writer.println(sender + ": " + message);
//             }
//         }
//     }
// }












// package server;

// import java.io.*;
// import java.net.*;
// import java.util.*;

// public class ClientHandler implements Runnable {
//     public static List<ClientHandler> allClients = new ArrayList<>();
//     private Socket socket;
//     private DataInputStream dis;
//     private DataOutputStream dos;
//     private String clientName;

//     public ClientHandler(Socket socket) {
//         try {
//             this.socket = socket;
//             this.dis = new DataInputStream(socket.getInputStream());
//             this.dos = new DataOutputStream(socket.getOutputStream());
            
//             this.clientName = dis.readUTF(); // Read username
//             allClients.add(this);
//             broadcastText("SERVER", clientName + " joined the chat.");
//         } catch (IOException e) { e.printStackTrace(); }
//     }

//     @Override
//     public void run() {
//         try {
//             while (true) {
//                 String type = dis.readUTF(); // Read command type: TEXT or FILE
                
//                 if (type.equals("TEXT")) {
//                     String content = dis.readUTF();
//                     DatabaseManager.saveMessage(clientName, content);
//                     broadcastText(clientName, content);
//                 } 
//                 else if (type.equals("FILE")) {
//                     String fileName = dis.readUTF();
//                     long size = dis.readLong();
//                     broadcastFile(clientName, fileName, size, dis);
//                 }
//             }
//         } catch (IOException e) {
//             allClients.remove(this);
//             try {
//                 broadcastText("SERVER", clientName + " left.");
//             } catch (IOException ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }

//     private void broadcastText(String sender, String msg) throws IOException {
//         for (ClientHandler client : allClients) {
//             client.dos.writeUTF("TEXT");
//             client.dos.writeUTF(sender + ": " + msg);
//         }
//     }

//     private void broadcastFile(String sender, String fileName, long size, DataInputStream sourceDis) throws IOException {
//         // Read file into memory buffer (Only for small/medium files for this example)
//         byte[] buffer = new byte[4096];
//         ByteArrayOutputStream baos = new ByteArrayOutputStream();
//         long remaining = size;
//         int read;
//         while (remaining > 0 && (read = sourceDis.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
//             baos.write(buffer, 0, read);
//             remaining -= read;
//         }
//         byte[] fileData = baos.toByteArray();

//         // Send to everyone else
//         for (ClientHandler client : allClients) {
//             if (client != this) {
//                 client.dos.writeUTF("FILE");
//                 client.dos.writeUTF(sender);
//                 client.dos.writeUTF(fileName);
//                 client.dos.writeLong(size);
//                 client.dos.write(fileData);
//             }
//         }
//     }
// }





package server;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    public static List<ClientHandler> allClients = new ArrayList<>();
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String clientName;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.clientName = dis.readUTF();
            allClients.add(this);
            broadcastText("SERVER", clientName + " joined.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String cmd = dis.readUTF(); 
                if (cmd.equals("TEXT")) {
                    String msg = dis.readUTF();
                    DatabaseManager.saveMessage(clientName, msg, null, null);
                    broadcastText(clientName, msg);
                } else if (cmd.equals("FILE")) {
                    String fileName = dis.readUTF();
                    long size = dis.readLong();
                    byte[] data = new byte[(int) size];
                    dis.readFully(data); // Important: Read all bytes

                    // SAVE TO DB
                    DatabaseManager.saveMessage(clientName, "FILE_ATTACHMENT", fileName, data);
                    
                    // BROADCAST TO ALL
                    broadcastFile(clientName, fileName, data);
                }
            }
        } catch (IOException e) {
            allClients.remove(this);
        }
    }

    private void broadcastText(String sender, String msg) throws IOException {
        for (ClientHandler c : allClients) {
            c.dos.writeUTF("TEXT");
            c.dos.writeUTF(sender);
            c.dos.writeUTF(msg);
        }
    }

    private void broadcastFile(String sender, String name, byte[] data) throws IOException {
        for (ClientHandler c : allClients) {
            c.dos.writeUTF("FILE");
            c.dos.writeUTF(sender);
            c.dos.writeUTF(name);
            c.dos.writeLong(data.length);
            c.dos.write(data);
        }
    }
}




