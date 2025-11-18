package funchat;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class GroupChat {

    // Keys are Members, and their values are their associated IP addresses.
    private final Map<String, String> members;

    public final MessageHistory messageHistory;
    private Set<Instant> oldMessageKeys;

    private Session session;
    private ClientEndpoint clientEndpoint;

    private final String username;
    private String host;

    // members is just TreeMap<Username, IPAddress::Port> <-- (build this from the discovery server)
    public GroupChat(String username, TreeMap<String, String> members) {
        this.username = username;
        this.members = members;
        this.messageHistory = new MessageHistory();
        this.oldMessageKeys = new TreeSet<>();
        this.host = null;
    }

    public String getUsername() {
        return this.username;
    }

    //Connects you to the host.
    public void determineHost() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        this.clientEndpoint = new ClientEndpoint(this.messageHistory);
        //The goal is to figure out who the 'server' should be
        boolean connectionMade = false;

        for (Map.Entry<String, String> entry : this.members.entrySet()) {
            String candidateUsername = entry.getKey();
            String routingInfo = entry.getValue();//ip:port
            String[] parts = routingInfo.split(":");
            String ip = parts[0];
            String port = parts[1];

            try {
                //Make sure this eventually times out
                URI uri = URI.create("ws://" + ip + ":" + port + "/host");
                this.session = container.connectToServer(clientEndpoint, uri);
                connectionMade = true;
                this.host = candidateUsername;
                System.out.println("determineHost: connected to " + candidateUsername +
                                   " session open=" + this.session.isOpen());
                break;
            } catch (DeploymentException | IOException e) {
                //try next member
                System.out.println("determineHost: failed to connect to " + candidateUsername +
                                   " at " + ip + ":" + port + " -> " + e.getMessage());
            }
        }

        //If no other active host was detected
        if (!connectionMade) {
            try {
                this.startHosting();
                System.out.println("'Fine, I'll do it myself' : user is now host");
            } catch (DeploymentException e) {
                throw new RuntimeException("Failed to start hosting", e);
            }
            //After starting host try to connect again
            this.determineHost();
        }
    }

    // Start acting as the host for this group chat
    private void startHosting() throws DeploymentException {
        String routingInfo = this.members.get(this.username);
        String[] parts = routingInfo.split(":");
        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);

        //copy current history 
        MessageHistory copiedMessageHistory = new MessageHistory(this.messageHistory);
        HostEndpoint.initShared(copiedMessageHistory, this);

        Server server = new Server(ip, port, "/", null, HostEndpoint.class);
        server.start();
        System.out.println("The server is started on " + ip + ":" + port);
    }


    public void sendMessage(Message message) {
        Instant sendTime = Instant.now();
        message.setTime_sent(sendTime);
        this.messageHistory.put(sendTime, message);

        if (this.clientEndpoint != null) {
            this.clientEndpoint.sendUpdate(Instant.MIN);
        } else {
            System.out.println("sendMessage: clientEndpoint is null; no host connection yet.");
        }
    }

    // Return ONLY the changes to the message history, SINCE
    // this method was last called.
    // point out we can't actively push updates because this is running native on JAVA.
    //
    public synchronized MessageHistory getMessageHistoryUpdated() {
        MessageHistory delta = new MessageHistory();

        for (Map.Entry<Instant, Message> entry : this.messageHistory.entrySet()) {
            Instant key = entry.getKey();
            if (!oldMessageKeys.contains(key)) {
                delta.put(key, entry.getValue());
            }
        }


        this.oldMessageKeys = new TreeSet<>(this.messageHistory.keySet());
        return delta;
    }

     public synchronized void reactToMessage(Instant msgTime, String username, String emoji) {
        Message m = this.messageHistory.get(msgTime);
        if (m == null) {
            System.out.println("reactToMessage: no message at " + msgTime);
            return;
        }

        m.addReaction(username, emoji);

        if (this.clientEndpoint != null) {
            this.clientEndpoint.sendUpdate(Instant.MIN);
        } else {
            System.out.println("reactToMessage: clientEndpoint is null; no host connection yet.");
        }
    }
}
