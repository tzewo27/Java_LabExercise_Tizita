// // Terminal 1: THE SERVER (The Brain)
// // This terminal stays open so you can see who joins and what messages are being saved.
// // Compile (Run once):javac -cp ".;../lib/*" server/*.java client/*.java
// // Run: java -cp ".;../lib/*" server.ChatServer 
// // Status: You should see "Waiting for clients..."

// // Terminal 2: CLIENT 1 (Abebe)
// // Run: java client.ChatClient
// // Action: Enter username "Abebe" in the popup. A window will appear.

// // Terminal 3: CLIENT 2 (Sara)
// // Run: java client.ChatClient
// // Action: Enter username "Sara" in the popup. A second window will appear.

// // Terminal 4: MAINTENANCE / DB CHECK (The "Pro" Terminal)
// // You keep this terminal free in case you need to:

// // Re-compile code if you make a change.
// // Check the database from the command line (if you don't want to use the browser).
// // Kill the port if you get the "Address already in use" error again.

// //netstat -ano | findstr :5000
// //taskkill /F /PID 7824

// package client;

// import javax.swing.*;
// import java.awt.*;
// import java.io.*;
// import java.net.*;

// public class ChatClient {
//     private BufferedReader reader;
//     private PrintWriter writer;
//     private JTextArea chatArea;
//     private JTextField inputField;
//     private String username;

//     public ChatClient() {
//         // 1. Ask for username using a popup
//         username = JOptionPane.showInputDialog("Enter your username:");
//         if (username == null || username.trim().isEmpty()) username = "Anonymous";

//         // 2. Setup the GUI Window
//         JFrame frame = new JFrame("Chat App - " + username);
//         chatArea = new JTextArea(20, 50);
//         chatArea.setEditable(false); // User can't type inside the chat history
//         chatArea.setLineWrap(true);

//         inputField = new JTextField(40);
//         JButton sendButton = new JButton("Send");

//         // When user hits Enter or clicks Send
//         sendButton.addActionListener(e -> sendMessage());
//         inputField.addActionListener(e -> sendMessage());

//         // Layout the components
//         frame.setLayout(new BorderLayout());
//         frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
//         JPanel bottomPanel = new JPanel();
//         bottomPanel.add(inputField);
//         bottomPanel.add(sendButton);
//         frame.add(bottomPanel, BorderLayout.SOUTH);

//         frame.pack();
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setVisible(true);

//         // 3. Connect to Server
//         connectToServer();
//     }

//     private void connectToServer() {
//         try {
//             Socket socket = new Socket("localhost", 5000);
//             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             writer = new PrintWriter(socket.getOutputStream(), true);

//             // Send username to server immediately
//             writer.println(username);

//             // 4. Thread to listen for incoming messages from server
//             new Thread(() -> {
//                 try {
//                     String message;
//                     while ((message = reader.readLine()) != null) {
//                         chatArea.append(message + "\n");
//                     }
//                 } catch (IOException e) {
//                     chatArea.append("Connection lost...");
//                 }
//             }).start();

//         } catch (IOException e) {
//             JOptionPane.showMessageDialog(null, "Could not connect to server.");
//             System.exit(0);
//         }
//     }

//     private void sendMessage() {
//         String text = inputField.getText();
//         if (!text.trim().isEmpty()) {
//             writer.println(text); // Send to server
//             chatArea.append("[" + username + "]: " + text + "\n"); // Show in your own window
//             inputField.setText(""); // Clear input
//         }
//     }

//     public static void main(String[] args) {
//         // Start the GUI on the Event Dispatch Thread (standard Java GUI practice)
//         SwingUtilities.invokeLater(ChatClient::new);
//     }
// }




// 





package client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private DataOutputStream dos;
    private DataInputStream dis;
    private JTextPane chatPane = new JTextPane();
    private StyledDocument doc = chatPane.getStyledDocument();
    private JTextField input = new JTextField(30);
    private String name;

    public ChatClient() {
        name = JOptionPane.showInputDialog("Enter Name:");
        if (name == null) System.exit(0);

        setTitle("Chat: " + name);
        chatPane.setEditable(false);
        add(new JScrollPane(chatPane), BorderLayout.CENTER);

        JPanel p = new JPanel();
        JButton send = new JButton("Send");
        JButton file = new JButton("📎");
        send.addActionListener(e -> sendText());
        file.addActionListener(e -> sendFile());
        p.add(input); p.add(send); p.add(file);
        add(p, BorderLayout.SOUTH);

        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        connect();
    }

    private void connect() {
        try {
            Socket s = new Socket("localhost", 5000);
            dos = new DataOutputStream(s.getOutputStream());
            dis = new DataInputStream(s.getInputStream());
            dos.writeUTF(name);

            new Thread(() -> {
                try {
                    while (true) {
                        String type = dis.readUTF();
                        String sender = dis.readUTF();
                        if (type.equals("TEXT")) {
                            String msg = dis.readUTF();
                            appendText(sender + ": " + msg);
                        } else if (type.equals("FILE")) {
                            receiveFile(sender);
                        }
                    }
                } catch (Exception e) { appendText("Disconnected from server."); }
            }).start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendText() {
        try {
            String msg = input.getText();
            if (!msg.isEmpty()) {
                dos.writeUTF("TEXT");
                dos.writeUTF(msg);
                input.setText("");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendFile() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            try {
                dos.writeUTF("FILE");
                dos.writeUTF(f.getName());
                dos.writeLong(f.length());
                FileInputStream fis = new FileInputStream(f);
                dos.write(fis.readAllBytes());
                fis.close();
                appendText("[System]: You sent a file: " + f.getName());
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void receiveFile(String sender) throws IOException {
        String fileName = dis.readUTF();
        long size = dis.readLong();
        byte[] data = new byte[(int) size];
        dis.readFully(data);

        // Save to downloads
        new File("downloads").mkdir();
        try (FileOutputStream fos = new FileOutputStream("downloads/" + fileName)) {
            fos.write(data);
        }

        appendText(sender + " sent a file: " + fileName);
        
        // Show Image preview
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            ImageIcon icon = new ImageIcon(data);
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            chatPane.setCaretPosition(doc.getLength());
            chatPane.insertIcon(new ImageIcon(img));
            appendText(""); 
        }
    }

    private void appendText(String s) {
        SwingUtilities.invokeLater(() -> {
            try { doc.insertString(doc.getLength(), s + "\n", null); } catch (Exception e) {}
        });
    }

    public static void main(String[] args) { new ChatClient(); }
}