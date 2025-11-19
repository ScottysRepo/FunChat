package funchat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import java.time.Instant;
import java.util.ArrayList;

public class MessageTextEncoder implements Encoder.Text<MessageHistory> {
    ObjectWriter objm ;
    public MessageTextEncoder(){
        ObjectMapper objm_tmp = new ObjectMapper();
        objm_tmp.registerModule(new JavaTimeModule());
        this.objm = objm_tmp.writerFor(MessageHistory.class).withDefaultPrettyPrinter();
    }
    @Override
    public void init(EndpointConfig ec) { }
    @Override
    public void destroy() {
    }

    @Override
    public String encode(MessageHistory message_history){
        try {
            String out = objm.writeValueAsString(message_history);
//            System.out.println("We encoded to: \n" + out);
            return out;
        }
        catch(Exception e)
        {
            System.out.println("we failed to encoder");
            e.printStackTrace();
            throw new RuntimeException("failed to encode message", e);
        }
    }
}
