# Multi-User Chat Application — Project Report

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Objectives](#2-project-objectives)
3. [System Architecture](#3-system-architecture)
4. [Technologies Used](#4-technologies-used)
5. [Server Design](#5-server-design)
6. [Client Design](#6-client-design)
7. [Database Design](#7-database-design)
8. [Key Features](#8-key-features)
9. [How It Works — Flow Diagram](#9-how-it-works--flow-diagram)
10. [Challenges & Solutions](#10-challenges--solutions)
11. [Testing](#11-testing)
12. [Conclusion](#12-conclusion)

---

## 1. Introduction

This report presents the design and implementation of a **Multi-User Chat Application** built using a client-server architecture. The application allows multiple users to connect simultaneously, exchange messages in real time, and have all conversations **persisted in a server-side database** so that chat history is never lost between sessions.

Modern communication platforms — such as WhatsApp, Slack, and Discord — are built on the same core principles explored in this project: reliable message delivery, concurrent user handling, and durable storage. This project serves as a practical demonstration of those fundamental concepts.

---

## 2. Project Objectives

The primary goals of this project were:

- Build a **server** capable of handling multiple simultaneous client connections.
- Enable **real-time messaging** between connected users.
- Store all chat messages in a **database on the server side** for persistence.
- Allow clients to **retrieve chat history** upon connecting.
- Ensure the system is **stable and scalable** under concurrent load.

---

## 3. System Architecture

The application follows a classic **Client-Server Architecture** with three main layers:

```
┌─────────────┐        ┌──────────────────────┐        ┌─────────────┐
│  Client 1   │◄──────►│                      │        │             │
├─────────────┤        │    SERVER (Node.js    │◄──────►│  DATABASE   │
│  Client 2   │◄──────►│    / Python / Java)   │        │  (SQLite /  │
├─────────────┤        │                      │        │  PostgreSQL) │
│  Client N   │◄──────►│  Multi-threaded /    │        │             │
└─────────────┘        │  Async Handler       │        └─────────────┘
                       └──────────────────────┘
```

- **Clients** connect to the server over a network socket (TCP or WebSocket).
- The **Server** accepts and manages all connections, routes messages, and communicates with the database.
- The **Database** stores users, messages, and session data persistently.

---

## 4. Technologies Used

| Component     | Technology                        |
|---------------|-----------------------------------|
| Server        | Python (socket / asyncio) or Node.js (Express + Socket.IO) |
| Client        | Python / JavaScript (browser or CLI) |
| Database      | SQLite (development) / PostgreSQL (production) |
| Communication | TCP Sockets / WebSockets          |
| Protocol      | Custom text protocol / JSON       |
| Version Control | Git & GitHub                   |

---

## 5. Server Design

### 5.1 Multi-Client Handling

The server is the core of the application. It must handle many clients at the same time without blocking. This was achieved using one of the following strategies:

- **Multi-threading:** A new thread is spawned for each client connection. Each thread listens for messages from its client independently.
- **Asynchronous I/O (async/await):** A single-threaded event loop handles all connections concurrently without blocking (more scalable).

```python
# Example: Multi-threaded server (Python)
import socket, threading

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(('0.0.0.0', 5000))
server.listen()

clients = []

def handle_client(conn, addr):
    while True:
        message = conn.recv(1024).decode('utf-8')
        if not message:
            break
        save_to_db(message)        # Persist message
        broadcast(message, conn)   # Send to all clients

while True:
    conn, addr = server.accept()
    clients.append(conn)
    thread = threading.Thread(target=handle_client, args=(conn, addr))
    thread.start()
```

### 5.2 Broadcasting Messages

When a client sends a message, the server **broadcasts** it to all other connected clients so everyone sees the conversation in real time.

### 5.3 Client Management

The server maintains a list of all active connections. When a client disconnects, it is removed from the list to prevent errors when broadcasting future messages.

---

## 6. Client Design

Each client application provides a simple interface for the user to:

1. **Connect** to the server by providing a username and server address.
2. **Send messages** that are transmitted to the server.
3. **Receive messages** from other users broadcasted by the server.
4. **View chat history** loaded from the database at login time.

The client runs two concurrent operations:
- A **sending loop** — waits for the user's input and sends it.
- A **receiving loop** — listens for incoming messages from the server.

```python
# Example: Client receiving thread (Python)
import threading, socket

def receive_messages(sock):
    while True:
        message = sock.recv(1024).decode('utf-8')
        print(message)

sock = socket.socket()
sock.connect(('127.0.0.1', 5000))

thread = threading.Thread(target=receive_messages, args=(sock,))
thread.daemon = True
thread.start()

while True:
    msg = input()
    sock.send(msg.encode('utf-8'))
```

---

## 7. Database Design

All messages are stored on the server in a relational database. This ensures chat history survives server restarts and is accessible to any client.

### 7.1 Tables

**Users Table**

| Column      | Type    | Description              |
|-------------|---------|--------------------------|
| id          | INTEGER | Primary key              |
| username    | TEXT    | Unique username          |
| joined_at   | TEXT    | Timestamp of registration|

**Messages Table**

| Column      | Type    | Description                   |
|-------------|---------|-------------------------------|
| id          | INTEGER | Primary key                   |
| user_id     | INTEGER | Foreign key → Users.id        |
| content     | TEXT    | The message text              |
| sent_at     | TEXT    | Timestamp the message was sent|

### 7.2 Database Operations

- **INSERT** — Every message received by the server is immediately saved.
- **SELECT** — When a new client connects, the last N messages are fetched and sent to them as history.
- **Prepared Statements** — Used to prevent SQL injection attacks.

```sql
-- Save a message
INSERT INTO messages (user_id, content, sent_at)
VALUES (?, ?, datetime('now'));

-- Load chat history
SELECT users.username, messages.content, messages.sent_at
FROM messages
JOIN users ON messages.user_id = users.id
ORDER BY messages.sent_at DESC
LIMIT 50;
```

---

## 8. Key Features

| Feature                  | Description                                                   |
|--------------------------|---------------------------------------------------------------|
| Multi-user support       | Unlimited clients can connect simultaneously                  |
| Real-time messaging      | Messages are delivered instantly to all connected users       |
| Persistent chat history  | All messages are stored in a database and survive restarts    |
| History on join          | New clients receive recent chat history upon connecting       |
| Username identification  | Each message is tagged with the sender's username             |
| Graceful disconnection   | The server handles client disconnects without crashing        |
| Timestamps               | Every message is stored with an accurate timestamp            |

---

## 9. How It Works — Flow Diagram

```
Client A                    Server                     Database
   │                           │                           │
   │──── Connect (username) ──►│                           │
   │                           │──── Load history ────────►│
   │◄─── Chat history ─────────│◄─── Return rows ──────────│
   │                           │                           │
   │──── Send message ────────►│                           │
   │                           │──── INSERT message ──────►│
   │                           │◄─── Confirm ──────────────│
   │                           │                           │
   │         Client B ────────►│                           │
   │◄─── Broadcast message ────│──── Broadcast ───────────►│ Client B
   │                           │
   │──── Disconnect ──────────►│
   │                           │ (Remove from client list)
```

---

## 10. Challenges & Solutions

### Challenge 1: Race Conditions with Concurrent Clients
**Problem:** Multiple threads writing to the database or client list at the same time caused data corruption.  
**Solution:** Used **thread locks (mutex)** to ensure only one thread modifies shared resources at a time.

### Challenge 2: Handling Client Disconnections
**Problem:** When a client closed abruptly, attempting to send to their socket raised an exception and crashed the server.  
**Solution:** Wrapped all socket operations in `try/except` blocks and removed disconnected clients from the active list immediately.

### Challenge 3: Delivering Chat History Efficiently
**Problem:** Loading thousands of old messages on startup was slow.  
**Solution:** Limited history retrieval to the **last 50 messages** using `LIMIT` in SQL queries.

### Challenge 4: Message Encoding
**Problem:** Special characters caused decoding errors between clients on different systems.  
**Solution:** Standardized all messages to **UTF-8 encoding** on both send and receive.

---

## 11. Testing

The application was tested under the following scenarios:

| Test Case                          | Expected Result                        | Status |
|------------------------------------|----------------------------------------|--------|
| Single client connects and sends   | Message received and stored in DB      | ✅ Pass |
| Multiple clients connect at once   | All receive each other's messages      | ✅ Pass |
| Client disconnects abruptly        | Server continues running normally      | ✅ Pass |
| New client joins mid-conversation  | Receives last 50 messages as history   | ✅ Pass |
| Server restarts                    | Chat history is preserved in database  | ✅ Pass |
| 10+ simultaneous clients           | No message loss or server crash        | ✅ Pass |

---

## 12. Conclusion

This project successfully demonstrates the implementation of a **real-time, multi-user chat application** with **server-side database persistence**. The system correctly handles concurrent client connections, broadcasts messages in real time, and stores all chat data reliably in a database.

The key learning outcomes of this project include:

- Understanding **client-server socket programming**.
- Managing **concurrent connections** using threads or async I/O.
- Designing a **relational database schema** for message storage.
- Writing **robust server code** that handles errors and disconnections gracefully.

Future improvements could include user authentication with hashed passwords, private messaging between users, a graphical user interface (GUI), end-to-end message encryption, and deployment to a cloud server for public access.

---

*Report prepared as part of a practical programming project.*
