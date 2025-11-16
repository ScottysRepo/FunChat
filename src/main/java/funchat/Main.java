package funchat;
import funchat.GroupChat;

import java.util.Map;
import java.util.TreeMap;
import java.util.*;
import java.io.*;
import java.time.Instant;

class UI
{
    public GroupChat groupChat;public UI(GroupChat groupChat)
    {
        this.groupChat = groupChat;
    }
    public void startConnection(){
        this.groupChat.determineHost();
    }

    public void runChat() throws IOException {
        MessageHistory oldMessageHistory = new MessageHistory();
        BufferedReader inputStream =  new BufferedReader(new InputStreamReader(System.in));
        while(true)
        {
            //Recieve messages:
            Set<Instant> keyCopy = new HashSet<>(this.groupChat.messageHistory.keySet());
            keyCopy.removeAll(oldMessageHistory.keySet());
            if(!keyCopy.isEmpty())
            {
                for(Instant time : keyCopy)
                {
                    System.out.println("<<<" + this.groupChat.messageHistory.get(time).getContents());
                }
                oldMessageHistory = new MessageHistory(this.groupChat.messageHistory);
            }
            // Send Messages

            if(inputStream.ready())
            {
                String line = inputStream.readLine();
                Message message = new Message(line, this.groupChat.getUsername());
                this.groupChat.sendMessage(message);
            }

        }
    }

}

public class Main {
    static TreeMap <String, String> userMap =     new TreeMap<>(Map.of(
            "user1", "127.0.0.1:8008",
            "user2", "127.0.0.1:6006"
    ));

    static void main(String[] args) throws InterruptedException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        String username = args[0];
        System.out.println("Instantiating Groupchat and UI for user " + username);

        GroupChat gc = new GroupChat(username, userMap);
        UI ui = new UI(gc);
        System.out.println("Establishing connection");
        ui.startConnection();
        System.out.println("Beginning chat");
        Thread.sleep(1000);
        try {
            ui.runChat();
        }
        catch (Exception e)
        {
            System.out.println("Something went horribly wrong");
            e.printStackTrace();
        }
    }
}
