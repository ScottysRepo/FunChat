package funchat;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

import java.time.Instant;


@jakarta.websocket.ClientEndpoint(
        encoders = {MessageTextEncoder.class},
        decoders = {MessageTextDecoder.class})
public class ClientEndpoint
{

    private MessageHistory messageHistory;
    private Session host_session;
    private GroupChat groupChat;

    public ClientEndpoint(MessageHistory messageHistory, GroupChat groupChat)
    {
        this.messageHistory = messageHistory;
        this.groupChat = groupChat;

    }

    @OnOpen
    public void onOpen(Session session)
    {
        this.host_session = session;
        System.out.println("A connection was made as a client");
    }

    @OnClose
    public void onClose(Session session)
    {
        System.out.println("Host migrated");
        groupChat.determineHost();
        //We need to exit to host migration somehow.
    }

    @OnMessage
    public void onMessage(MessageHistory msg, Session session)
    {
        this.messageHistory.mergeHistories(msg);
    }



    public void sendUpdate(Instant time)
    {
        this.host_session.getAsyncRemote().sendObject(this.messageHistory.historyAfter(time));
    }

}
