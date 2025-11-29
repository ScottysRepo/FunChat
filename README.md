# FunChat

## Table of Contents
- [Getting Started](#1-getting-started)
    - [Client-Server backend](#11-client-server-backend)
    - [Peer-to-Peer backend](#12-peer-to-peer-backend)
    - [Frontend](#13-frontend)
- [Test Sample](#2-test-sample)
- [Architecture style 1: Client-Server](#3-architecture-style-1-client-server)
- [Architecture style 2: Peer-to-Peer](#4-architecture-style-2-peer-to-peer)
- [Architecture Style Comparison](#5-architecture-style-comparison)


## 1. Getting Started

---

### 1.1 Client-Server backend

This is the backend of FunChat implemented by using Client Server architecture style.

Prerequisites:

- Java 21
- Maven
- PostgreSQL (database)
- SwaggerUI

---

#### (a). Clone the repository

```bash
git clone https://github.com/ScottysRepo/FunChat.git
cd FunChat
cd Selected
cd FunChat-client-server-backend

npm install
```
- Open FunChat-client-server-backend through IntelliJ IDE.
- Download Lombok plugin in IntelliJ IDE.
- Sometimes IntelliJ may ask you to enable the "Enable annotation processing" option

#### (b). Create Database
Create database in PostgreSQL named fun_chat.
Update the `application.yml` file with your PostgreSQL database credentials.
```properties
spring:
    datasource:
        url: jdbc:postgresql://localhost:5432/hask_task
        username: postgres
        password: postgres1
```

#### (c). Run the program
Run the FunchatApplication class.

#### (d). Swagger UI
Use http://localhost:8080/swagger-ui/index.html to check API.

#### (e). Run the React UI in a new terminal
```bash
cd FunChat
cd Selected
cd FunChat-client-server-backend/frontendtesting
npm install
cd my-react-login
npm run dev
```
---

### 1.2 Peer-to-Peer backend

This is the peer-to-peer version of FunChat implemented using a decentralized GroupChat architecture
with a Spring Boot–based discovery server and a JavaFX desktop client.

- Java 21 or higher
- Maven 3.8 or higher
- JavaFX
- VSCode
  
#### (a). Clone the repository
```bash
git clone https://github.com/ScottysRepo/FunChat.git
cd FunChat
cd Unselected
```


#### (b). Start the Discovery Server
```bash
cd FunChat-discovery-server
mvn spring-boot:run
```
 
#### (c). Start the peer to peer backend & JavaFX GUI in a new terminal
```bash
cd FunChat
cd Unselected
cd FunChat-peer-to-peer-backend
mvn javafx:run
```  
---

## 2. Testing

### 2.1 Client-Server backend

- Open two browsers and inside of each window click on "Need an account? Register"
- Create a username and password for each browser then log in
- Click on "Open Chats" inside of one window, then click on "New Chat"
- You can choose to create a group chat or DM. You can also choose to give the group a name.
- In the "Member IDs" type in the ID number(s) your other test account, then click create.
- Type a test message in the message field, then click "Send."
- Inside the other test account window, look for the new DM or group to appear, or you may need to click on "Refresh Chats" depending on your internet connection.
- In the other test account, once you see a message has appeared you can respond to it, or react with an emoji by clicking on the emojis under the message.


### 2.2 Peer to Peer

- After starting the Discovery Server, open two (or more) new terminals to run the GUI, and inside of each GUI that opens click on "Need an account? Register"
- In each window, create a username when prompted
- When prompted for a port, enter 8080 for the first, 8081 for the second, etc.
- You can choose to create a new direct message or new group chat
- When prompted for your peer's username, choose the name of the other test account
- Type a test message in the message field, then click "Send."
- Inside the other test account window, look for the new DM or group to appear, or you may need to initiate a DM/group chat, and send a message before you can see their message, depending on your internet connection.
- In the other test account, once you see a message has appeared you can respond to it, or react with an emoji by right clicking on the message, then choosing an emoji.

---

## 3. Architecture style 1: Client-Server

The backend follows a Client–Server architecture, where the client (web or mobile frontend) interacts with a centralized Spring Boot server via REST APIs and WebSocket channels.
The server encapsulates all core business logic, persistence operations, authentication, and real-time messaging.

Key Principles: 
- The client sends HTTP or WebSocket requests.
- The server processes logic through controllers → services → repositories.
- Entities represent database tables.
- WebSocket enables real-time group chat updates.

### 3.1 Configuration Layer
`WebSocketConfig`

Enables Spring WebSocket + STOMP messaging.
    
Defines WebSocket endpoints (/funchat-websocket).

Configures message broker (/topic).

Acts as the connector enabling real-time communication between backend and browsers.

### 3.2 Controller Layer
Controllers are connectors handling interactions between the client and the server.

`AuthController`

Exposes authentication endpoints (/login, /register).

Issues JWT tokens for secure communication.

`GroupController`

Manages group CRUD operations.

Provides REST endpoints for group-related data retrieval.

`MessageController`

Handles messages CRUD operations.

Publishes new messages to WebSocket topics (/topic/group/{groupId}).

Bridges REST → Service → WebSocket publish pipeline.

### 3.3 Service Layer
Services are components implementing core application logic.

`AuthService`

Performs user verification.

Generates/validates JWT.

Hashes and stores passwords.

`GroupService`

Manages group creation, member lists.

Implements group-related business rules.

`MessageService`

Saves messages into the database.

Manages emotes on messages.

Returns message entities to controller.

Decides when to publish real-time updates via WebSocket.

### 3.4 Repository Layer

Repositories are connectors to the database.

`GroupRepository`

Performs CRUD on groups.

`MessageRepository`

Saves and retrieves messages for specific groups.

`UserRepository`

Looks up users during login/register.

### 3.5 Domain Model Layer
Entities are components representing persistent objects stored in the database.

`UserEntity`

Represents application users.

Stores username, password.

`GroupEntity`

Represents a chat group.

Holds group metadata and member relationships.

`MessageEntity`

Represents a chat message.

Includes senderId, groupId, content, timestamp.

Contains map of emote counts using JPA mapping.

### 3.6 Utility Layer
`JwtUtil`

Handles JWT token generation and verification.

Used by AuthService and security filters.

### 3.7 Connectors
| Connector Type        | Example in Project             | Function                                          |
|-----------------------| --------------------------------- | ------------------------------------------------- |
| **HTTP / REST**       | Controllers                       | Client requests (login, send message, get groups) |
| **WebSocket / STOMP** | WebSocketConfig + MessageController | Real-time message updates to all group members    |
| **Repository (JPA)**  | UserRepository, MessageRepository | Bridge between service layer and database         |
| **Procedure calls**   | Controller → Service              | Internal connector providing business logic flow  |


## 4. Architecture style 2: Peer-to-Peer

//ToDo


## 5. Architecture Style Comparison
In the early design phase of the project, we evaluated two candidate architecture styles for implementing the group chat system:

(1) Client–Server Architecture, and

(2) Peer-to-Peer (P2P) Architecture.

This section elaborates on the differences between the two styles, compares their trade-offs, and explains the rationale behind selecting the Client–Server model for the final implementation.

--- 

### 5.1 Candidate Architecture Style 1: Client–Server

**Characteristics**

* Centralized server handles all messages and persists data.
* Clients do not communicate with each other directly.
* WebSocket broker manages real-time updates.
* Database is managed by a single backend.

**Strengths**

* Simplified Development
* Reliable Message Delivery
* Ease of Persistence and History Retrieval
* Easier to Scale Horizontally
* Better fault tolerance and maintenance

**Weaknesses**
* Requires server hosting.
* Higher load on server when number of users grows.
* Single point of failure unless replicated.

### 5.2 Candidate Architecture Style 2: Peer-to-Peer

//ToDo

### 5.3 Comparison Summary

| Criteria                    | Client–Server                  | Peer-to-Peer                 |
| --------------------------- | ------------------------------ | ---------------------------- |
| **Development Complexity**  | Low                            | Very High                    |
| **Real-time messaging**     | Easy via WebSocket             | Hard (WebRTC, NAT traversal) |
| **Message Ordering**        | Guaranteed                     | Hard to maintain             |
| **Message History Storage** | Centralized & consistent       | Distributed & inconsistent   |
| **Security**                | Strong (JWT, server filtering) | Weak (no central authority)  |
| **Scalability**             | Horizontal scaling possible    | Poor for large groups        |
| **Reliability**             | High                           | Depends on peers             |
