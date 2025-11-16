package funchat;
import funchat.Message;

import java.time.Instant;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;


// Tree Map ( time sent , the message itself)

public class MessageHistory extends  TreeMap<Instant, Message> {

    Instant lastUpdate = java.time.Instant.now();
    Instant oldestUpdate = Instant.MAX;

    public MessageHistory(){super();}
    public MessageHistory(TreeMap<Instant, Message> messageTreeMap){super(messageTreeMap);}
    public MessageHistory(MessageHistory messageHistory){super(messageHistory.tailMap(Instant.MIN));}

    public void mergeHistories(MessageHistory history)
    {
        Set<Instant> new_keys = new HashSet<>(history.keySet());
        new_keys.removeAll(this.keySet());
        Instant oldest_time = this.oldestUpdate;
        for(Instant key : new_keys)
        {
            Message msg = history.get(key);
            msg.logArrival();
            this.put(key, msg);
            if(key.compareTo( oldest_time)>0){oldest_time = key;}
        }
        lastUpdate = java.time.Instant.now();
        oldestUpdate = oldest_time;
    }

    public MessageHistory historyAfter(Instant time)
    {
        MessageHistory tmp = new MessageHistory();
        tmp.putAll(this.tailMap(time));
        return tmp;
    }
}
