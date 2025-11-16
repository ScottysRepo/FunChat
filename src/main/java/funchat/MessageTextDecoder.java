package funchat;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import java.util.ArrayList;

import static funchat.HostEndpoint.messageHistory;

public class MessageTextDecoder implements Decoder.Text<MessageHistory> {
    private ObjectReader objr;
    public MessageTextDecoder(){
        ObjectMapper temp_objm = new ObjectMapper();
        temp_objm.registerModule(new JavaTimeModule());
        objr = temp_objm.readerFor(MessageHistory.class);

    };
    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public void destroy() {
    }


    @Override
    public MessageHistory decode(String s) throws DecodeException {
        try {
            MessageHistory messageHistory = objr.readValue(s);
            return messageHistory;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DecodeException(s, "Something went horribly wrong", e);
        }

    }

    @Override
    public boolean willDecode(String s) {
//        ObjectReader objr = new ObjectMapper().reader();
//        try {
//            MessageHistory message = (MessageHistory) objr.readValue(s);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
        return true;
    }
}
