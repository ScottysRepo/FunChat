package funchat;

import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;

import java.time.Instant;
import java.util.function.Consumer;

@jakarta.websocket.ClientEndpoint(
        encoders = {MessageTextEncoder.class},
        decoders = {MessageTextDecoder.class})
public class ClientEndpoint {

    private final MessageHistory messageHistory;
    private final Consumer<MessageHistory> onHistoryUpdated;
    private Session host_session;

    public ClientEndpoint(MessageHistory messageHistory) {
        this(messageHistory, null);
    }

    public ClientEndpoint(MessageHistory messageHistory,
                          Consumer<MessageHistory> onHistoryUpdated) {
        this.messageHistory = messageHistory;
        this.onHistoryUpdated = onHistoryUpdated;
    }


    @OnMessage
    public void onMessage(MessageHistory msg, Session session) {
        this.messageHistory.mergeHistories(msg);

        if (onHistoryUpdated != null) {
            onHistoryUpdated.accept(this.messageHistory);
        }
    }

    public void sendUpdate(Instant time) {
        this.host_session.getAsyncRemote()
                .sendObject(this.messageHistory.historyAfter(time));
    }

    @jakarta.websocket.OnOpen
    public void onOpen(Session session) {
        this.host_session = session;
    }

    @jakarta.websocket.OnClose
    public void onClose(Session session) {
        this.host_session = null;
    }
}
