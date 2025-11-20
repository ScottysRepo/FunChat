package funchat;

import java.time.Instant;
import java.util.TreeMap;
import java.util.Map;

// Tree Map... time sent and the message itself
public class MessageHistory extends TreeMap<Instant, Message> {

    public MessageHistory() {
        super();
    }

    // Copy constructor
    public MessageHistory(MessageHistory other) {
        super(other);
    }

    public void mergeHistories(MessageHistory other) {
        for (Map.Entry<Instant, Message> entry : other.entrySet()) {
            Instant key = entry.getKey();
            Message message = entry.getValue();

            // Overwriting existing message so the emote reactions actually show up
            this.put(key, message);
        }
    }

    public MessageHistory historyAfter(Instant time) {
        MessageHistory result = new MessageHistory();
        for (Map.Entry<Instant, Message> entry : this.entrySet()) {
            if (!entry.getKey().isBefore(time)) { // >= time
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}