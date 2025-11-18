package funchat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private String contents;
    private Instant time_sent;

    // Sender username
    private String username;

    // Map: username -> emoji
    private Map<String, String> reactions = new HashMap<>();

    // Required by JSON (Jackson)
    public Message() {
    }

    // Convenience constructor
    public Message(String contents, String username) {
        this.contents = contents;
        this.username = username;
        this.time_sent = Instant.now();
    }

    // --- Contents ---
    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    // --- Timestamp ---
    public Instant getTime_sent() {
        return time_sent;
    }

    public void setTime_sent(Instant time_sent) {
        this.time_sent = time_sent;
    }

    // --- Username ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // --- Reactions ---
    public Map<String, String> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, String> reactions) {
        this.reactions = reactions;
    }

    /**
     * Add or change a reaction.
     * A user can have only one active emoji at a time.
     */
    public void addReaction(String username, String emoji) {
        if (this.reactions == null) {
            this.reactions = new HashMap<>();
        }
        this.reactions.put(username, emoji);
    }

    /**
     * Remove a user's reaction (optional helper).
     */
    public void removeReaction(String username) {
        if (this.reactions != null) {
            this.reactions.remove(username);
        }
    }

    @Override
    public String toString() {
        return "Message{username=" + username +
                ", contents=" + contents +
                ", time_sent=" + time_sent +
                ", reactions=" + reactions + "}";
    }
}
