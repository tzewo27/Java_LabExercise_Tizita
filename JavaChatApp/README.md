# Multi-User Chat Application — Project Report

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Objectives](#2-project-objectives)
3. [System Overview](#3-system-overview)
4. [Technologies Used](#4-technologies-used)
5. [Application Architecture](#5-application-architecture)
6. [Server Design](#6-server-design)
7. [Client Design](#7-client-design)
8. [Database Storage](#8-database-storage)
9. [File Transfer](#9-file-transfer)
10. [Challenges & Solutions](#10-challenges--solutions)
11. [Testing](#11-testing)
12. [Conclusion](#12-conclusion)

---

## 1. Introduction

This report presents the design and implementation of a **Multi-User Chat Application** written in **Java**. The application uses a client-server architecture where multiple clients can connect to a single server, exchange text messages in real time, and **transfer files** to each other. All chat messages are stored in a **server-side database** so the conversation history is never lost.

---

## 2. Project Objectives

- Build a **Java server** that handles multiple connected clients at the same time.
- Allow clients to **send and receive text messages** in real time.
- Store all messages in a **database on the server**.
- Support **file transfer** between connected clients.
- Keep the design clean and straightforward.

---

## 3. System Overview

```
┌──────────┐          ┌─────────────────────┐          ┌──────────────┐
│ Client 1 │◄────────►│                     │          │              │
├──────────┤  Socket  │   Java Server        │◄────────►│   Database   │
│ Client 2 │◄────────►│   (Multi-threaded)  │          │  (SQLite)    │
├──────────┤          │                     │          │              │
│ Client N │◄────────►│                     │          └──────────────┘
└──────────┘          └─────────────────────┘
```

- Clients connect to the server using **Java Sockets**.
- The server runs a **separate thread** for each connected client.
- All text messages are saved to a **SQLite database**.
- Files are sent as **raw bytes** through the socket and saved on the receiving side.

---

## 4. Technologies Used

| Component     | Technology                        |
|---------------|-----------------------------------|
| Language      | Java (JDK 17+)                    |
| Networking    | `java.net.Socket` / `ServerSocket`|
| Multithreading| `java.lang.Thread`                |
| Database      | SQLite via JDBC (`sqlite-jdbc`)   |
| File Transfer | `java.io` (InputStream / OutputStream) |
| IDE           | IntelliJ IDEA / Eclipse           |

---

## 5. Application Architecture

The project has three main classes:

| Class           | Role                                                    |
|-----------------|---------------------------------------------------------|
| `Server.java`   | Accepts client connections, manages threads, saves messages to DB |
| `ClientHandler.java` | Runs on the server — handles one client's messages and files |
| `Client.java`   | Connects to the server, sends/receives messages and files |

---

## 6. Server Design

The server listens on a fixed port. For every client that connects, it starts a new `ClientHandler` thread so all clients are handled at the same time without blocking each other.

```java
ServerSocket serverSocket = new ServerSocket(5000);
System.out.println("Server started on port 5000");

while (true) {
    Socket clientSocket = serverSocket.accept();
    ClientHandler handler = new ClientHandler(clientSocket);
    new Thread(handler).start();
}
```

### ClientHandler

Each `ClientHandler` reads incoming data from its client. If the data is a text message, it broadcasts it to all other clients and saves it to the database. If it is a file, it receives the file bytes and forwards them to the target client.

```java
public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public void run() {
        try {
            input  = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String type = input.readUTF(); // "MSG" or "FILE"

                if (type.equals("MSG")) {
                    String message = input.readUTF();
                    saveToDatabase(message);
                    broadcast(message);

                } else if (type.equals("FILE")) {
                    String fileName = input.readUTF();
                    long fileSize   = input.readLong();
                    byte[] fileData = input.readNBytes((int) fileSize);
                    broadcast(fileName, fileData); // forward file to others
                }
            }
        } catch (IOException e) {
            removeClient(this);
        }
    }
}
```

### Broadcasting

The server keeps a list of all connected `ClientHandler` objects. When a message or file arrives, it is sent to every client in the list except the sender.

```java
static List<ClientHandler> clients = new ArrayList<>();

void broadcast(String message) {
    for (ClientHandler client : clients) {
        if (client != this) {
            client.output.writeUTF("MSG");
            client.output.writeUTF(message);
        }
    }
}
```

---

## 7. Client Design

The client connects to the server and then runs two threads — one for **sending** and one for **receiving** — so both can happen at the same time without blocking.

```java
Socket socket = new Socket("127.0.0.1", 5000);
DataInputStream  input  = new DataInputStream(socket.getInputStream());
DataOutputStream output = new DataOutputStream(socket.getOutputStream());

// Receiving thread
new Thread(() -> {
    while (true) {
        String type = input.readUTF();
        if (type.equals("MSG")) {
            System.out.println(input.readUTF());
        } else if (type.equals("FILE")) {
            String name = input.readUTF();
            long size   = input.readLong();
            byte[] data = input.readNBytes((int) size);
            Files.write(Path.of("received_" + name), data);
            System.out.println("File received: " + name);
        }
    }
}).start();

// Sending (main thread)
Scanner scanner = new Scanner(System.in);
while (scanner.hasNextLine()) {
    String line = scanner.nextLine();
    output.writeUTF("MSG");
    output.writeUTF(line);
}
```

---

## 8. Database Storage

The server stores every chat message in a **SQLite database** using JDBC. This means all messages are persisted even if the server restarts.

### Messages Table

| Column     | Type    | Description                  |
|------------|---------|------------------------------|
| id         | INTEGER | Primary key (auto increment) |
| sender     | TEXT    | Username of the sender       |
| content    | TEXT    | The message text             |
| sent_at    | TEXT    | Timestamp of the message     |

### Saving a Message

```java
void saveToDatabase(String sender, String message) {
    String sql = "INSERT INTO messages (sender, content, sent_at) VALUES (?, ?, datetime('now'))";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, sender);
        stmt.setString(2, message);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

When a new client connects, the server loads the last 50 messages from the database and sends them to the client as chat history.

---

## 9. File Transfer

File transfer works by sending the file as raw bytes through the same socket connection used for text messages. A simple protocol tag (`"FILE"` vs `"MSG"`) tells the receiver what kind of data is coming next.

### Sending a File (Client Side)

```java
void sendFile(String filePath) throws IOException {
    File file     = new File(filePath);
    byte[] data   = Files.readAllBytes(file.toPath());

    output.writeUTF("FILE");           // Type tag
    output.writeUTF(file.getName());   // File name
    output.writeLong(data.length);     // File size in bytes
    output.write(data);                // Raw file bytes
    output.flush();
}
```

### Receiving a File (Client Side)

```java
String name = input.readUTF();
long size   = input.readLong();
byte[] data = input.readNBytes((int) size);
Files.write(Path.of("received_" + name), data);
System.out.println("File saved as: received_" + name);
```

The server receives the file from the sender and forwards it to all other connected clients using the same format. Received files are saved automatically in the client's working directory.

---

## 10. Challenges & Solutions

### Challenge 1: Handling Multiple Clients at the Same Time
**Problem:** The server could only talk to one client at a time if handled in a single thread.  
**Solution:** Created a new `Thread` for each connecting client using `ClientHandler`, allowing the server to manage all clients in parallel.

### Challenge 2: Distinguishing Messages from Files
**Problem:** Both text and file data travel through the same socket, so the receiver needed to know which type was arriving.  
**Solution:** Added a type tag (`"MSG"` or `"FILE"`) written before every piece of data using `writeUTF()`. The receiver reads this tag first and then handles the data accordingly.

### Challenge 3: Client Disconnecting Unexpectedly
**Problem:** If a client closed abruptly, the server threw an `IOException` and could crash.  
**Solution:** Wrapped the client handler loop in a `try/catch`. When an exception is caught, the client is removed from the active clients list and the thread ends cleanly.

### Challenge 4: Large File Transfers
**Problem:** Very large files caused memory issues when loaded entirely into a byte array.  
**Solution:** For large files, the data can be sent in **chunks** using a buffer loop instead of loading the whole file at once, reducing memory usage significantly.

```java
// Chunked transfer example
byte[] buffer = new byte[4096];
int bytesRead;
while ((bytesRead = fileStream.read(buffer)) != -1) {
    output.write(buffer, 0, bytesRead);
}
```

---

## 11. Testing

| Test Case                                    | Expected Result                                      | Status  |
|----------------------------------------------|------------------------------------------------------|---------|
| Start the server                             | Server starts and listens on port 5000               | ✅ Pass |
| Connect one client                           | Client connects successfully                         | ✅ Pass |
| Connect multiple clients at the same time    | All clients connect without errors                   | ✅ Pass |
| One client sends a text message              | All other clients receive the message                | ✅ Pass |
| Message is saved to the database             | Message appears in the SQLite messages table         | ✅ Pass |
| New client joins and receives history        | Last 50 messages are sent to the new client          | ✅ Pass |
| Client sends a file                          | All other clients receive and save the file          | ✅ Pass |
| Client disconnects abruptly                  | Server continues running; other clients unaffected   | ✅ Pass |
| Server restarts                              | Chat history is still available from the database    | ✅ Pass |

---

## 12. Conclusion

This project successfully implements a **multi-user Java chat application** that supports real-time messaging, server-side message storage, and file transfer between clients. The server handles multiple clients simultaneously using threads, and a simple type-tag protocol cleanly separates text messages from file transfers over the same socket connection.

Key learning outcomes include:

- Using **Java Sockets** for network communication.
- Managing **concurrent clients** with multithreading.
- Persisting data with **SQLite and JDBC**.
- Implementing a **binary file transfer** protocol over a socket.
- Writing **robust server code** that handles client disconnections gracefully.

Future improvements could include a graphical user interface (GUI), private messaging between specific users, progress indicators for file transfers, and user login with a username and password.

---

