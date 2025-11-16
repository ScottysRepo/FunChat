package funchat;
import java.io.Serializable;
import java.time.Instant;

// Make this as similar to Linwei's version of the Message object as possible.
public class Message implements Serializable{
    private String contents;
    private Instant time_sent;
    private Instant time_recieved;
    private String username;

    public Message(String contents, String username)
    {
        this.contents = contents;
        this.username = username;
    }

    public Message()
    {
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
    public void setUsername(String username){this.username = username;}

    public Instant getTime_sent() {
        return time_sent;
    }

    public void setTime_sent(Instant time){this.time_sent = time;}

    public void logArrival()
    {
//        assert(this.time_recieved == null);
        this.time_recieved = Instant.now();
    }

    public Instant getTime_recieved() {
        return time_recieved;
    }

    public void setTime_recieved(Instant time_recieved) {
        this.time_recieved = time_recieved;
    }


    public String getContents() {
        return contents;
    }


}
