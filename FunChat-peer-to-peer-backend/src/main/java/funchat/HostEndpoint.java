package funchat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import funchat.MessageTextEncoder;
import funchat.MessageTextDecoder;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint(value = "/host",
        encoders = {MessageTextEncoder.class},
        decoders = {MessageTextDecoder.class})
public class HostEndpoint {
    String IPAddress;
    int port;
    int max_connections;
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    static MessageHistory messageHistory;
    static GroupChat groupChat;

    static void initShared(MessageHistory messageHistory, GroupChat groupChat)
    {
        HostEndpoint.messageHistory = messageHistory;
        HostEndpoint.groupChat = groupChat;

    }




    @OnOpen
    public void onOpen(Session session)
    {
        this.sessions.add(session);
    }

    @OnClose
    public void onClose(Session session)
    {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(MessageHistory msg, Session session)
    {
        messageHistory.mergeHistories(msg);
        this.sendUpdate(Instant.MIN);
    }



    public void sendUpdate(Instant time)
    {
        for(Session s : this.sessions)
        {
            s.getAsyncRemote().sendObject(messageHistory.historyAfter(time));
        }
    }



}
