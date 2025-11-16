package funchat;

import java.lang.reflect.Array;
import java.util.*;

import funchat.Message;
import funchat.MessageHistory;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.time.Duration;
import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;

 import com.fasterxml.jackson.databind.*;


 //It's okay if they're similar.
 //


// Save this object or figure out a way to make it persistent

public class GroupChat {
    // Keys are Members, and their values are their associated IP addresses.
    private Map<String, String> members;
    public MessageHistory messageHistory;
    private Set<Instant> oldMessageKeys;
    Session session;
    ClientEndpoint clientEndpoint;

    private String username;
    private String host;



    // members is just TreeMap<Username, IPAddress::Port> <-- (build this from the discovery server)
    public GroupChat(String username, TreeMap<String, String> members) //members INCLUDES user.
    {
        this.username = username;
        this.members = members;
        this.messageHistory = new MessageHistory();
        this.oldMessageKeys = new TreeSet<Instant>();
        this.host = null;
    }

    //Connects you to the host.
    public void determineHost() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.clientEndpoint = new ClientEndpoint(this.messageHistory,this);
        //The goal is to figure out who the 'server' should be
        boolean connectionMade = false;
        for(Map.Entry<String  , String> entry : this.members.entrySet())
        {


            String username = entry.getKey();
            String port = entry.getValue().split(":")[1];
            String ip = entry.getValue().split(":")[0];

            try {
                //Make sure this eventually times out
                this.session = container.connectToServer(clientEndpoint, URI.create("ws://" + ip + ":" + port + "/host" ));
                connectionMade = true;
                this.host = username;
                System.out.println("determineHost: connectToServer returned, session open=" + this.session.isOpen());
                break;
            }
            catch(DeploymentException | IOException e)
            {
                continue;
            }

        }

        //If no other active host was detected
    if (!connectionMade)
    {
        try {
            this.startHosting();
            System.out.println("'Fine, I'll do it myself' : user is now host");

        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
        this.determineHost();
    }
    }
    private void startHosting() throws DeploymentException {
        String routing_info = this.members.get(this.username);
        Integer port = Integer.parseInt(routing_info.split(":")[1]);
        String ip = routing_info.split(":")[0];
        MessageHistory copiedMessageHistory = new MessageHistory(this.messageHistory);
        HostEndpoint.initShared(copiedMessageHistory,this);
        Server server = new Server(ip, port, "/", null, HostEndpoint.class);
        server.start();
        System.out.println("The server is started");
    }

    public String getUsername(){return this.username;}

    //Todo try to merge your Message object with LinWei's (extends?)
    public void sendMessage(Message message)
    {
        Instant send_time = Instant.now();
        message.setTime_sent(send_time);
        this.messageHistory.put(send_time, message);
        this.clientEndpoint.sendUpdate(Instant.MIN);
    }

    // Return ONLY the changes to the message history, SINCE
    // this method was last called.
    // point out we can't actively push updates because this is running native on JAVA.
    //
    public MessageHistory getMessageHistoryUpdated()
    {
        //TODO: Actually fix this to handle different stuff.
        MessageHistory tmp = new MessageHistory(this.messageHistory);
        for(Instant key : this.oldMessageKeys){
        tmp.remove(this.oldMessageKeys);
        }
        this.oldMessageKeys = new TreeSet<Instant>(this.messageHistory.keySet());
        return tmp;
    }

    //TODO: all stuff with Emojis
    //TODO: actually testing that this works in the command line.
    //TODO: making a big class that integrates all this stuff. (Like the discovery Server) (look at front end).
    //TODO: make code about Group Chat.

}
