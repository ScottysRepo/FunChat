package funchat.ui;

import funchat.GroupChat;
import funchat.Message;
import funchat.MessageHistory;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class ChatClientService {

    private final Consumer<MessageHistory> historyConsumer;
    private GroupChat groupChat;
    private String username;

    public ChatClientService(Consumer<MessageHistory> historyConsumer) {
        this.historyConsumer = historyConsumer;
    }

    /**
     * Connect to a specific group chat with the given member map,
     * seeding the initial local history (e.g., from disk).
     * members: username -> "ip:port"
     */
    public void connect(String username,
                        TreeMap<String, String> members,
                        MessageHistory initialHistory) {
        this.username = username;

        // Make sure we are included in the group
        if (!members.containsKey(username)) {
            // Default to localhost:8080 if user forgot to add themselves
            members.put(username, "localhost:8080");
        }

        // Use your existing GroupChat logic
        groupChat = new GroupChat(username, members);

        // 🆕 Seed the in-memory history with whatever we loaded from disk
        if (initialHistory != null && !initialHistory.isEmpty()) {
            groupChat.messageHistory.putAll(initialHistory);
        }

        groupChat.determineHost();  // will start hosting if no one is available

        // Poll for latest history and push to UI, regardless of deltas
        Thread poller = new Thread(() -> {
            try {
                while (true) {
                    if (groupChat != null && groupChat.messageHistory != null) {
                        // Always push current full history to UI
                        historyConsumer.accept(groupChat.messageHistory);
                    }
                    Thread.sleep(300); // ~3x per second
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        poller.setDaemon(true);
        poller.start();
    }

    /**
     * Connect without initial history (fresh chat).
     */
    public void connect(String username, TreeMap<String, String> members) {
        connect(username, members, null);
    }

    /**
     * Convenience overload: single-user "group" on localhost:8080.
     */
    public void connect(String username) {
        TreeMap<String, String> members = new TreeMap<>();
        members.put(username, "localhost:8080");
        connect(username, members, null);
    }

    /**
     * Send a message using GroupChat.
     */
    public void sendMessage(String text) {
        if (groupChat == null) {
            System.err.println("GroupChat not initialized; call connect() first.");
            return;
        }

        Message m = new Message();
        m.setContents(text);
        m.setUsername(username);
        m.setTime_sent(Instant.now());

        groupChat.sendMessage(m);
    }

    /**
     * React to message by its index in the history (same order as UI list).
     */
    public void reactToMessageAtIndex(int index, String emoji) {
        if (groupChat == null) {
            System.err.println("GroupChat not initialized; call connect() first.");
            return;
        }

        int i = 0;
        for (Map.Entry<Instant, Message> entry : groupChat.messageHistory.entrySet()) {
            if (i == index) {
                Instant msgTime = entry.getKey();
                groupChat.reactToMessage(msgTime, username, emoji);
                return;
            }
            i++;
        }

        System.out.println("reactToMessageAtIndex: no message at index " + index);
    }

    /**
     * Legacy helper if you still reference this somewhere.
     */
    public void reactToMessage(Instant msgTime, String emoji) {
        if (groupChat == null) {
            System.err.println("GroupChat not initialized; call connect() first.");
            return;
        }
        groupChat.reactToMessage(msgTime, username, emoji);
    }
}
