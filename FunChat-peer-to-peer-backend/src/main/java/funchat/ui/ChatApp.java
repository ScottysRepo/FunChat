package funchat.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import funchat.Message;
import funchat.MessageHistory;
import funchat.discovery.DiscoveryClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatApp extends Application {

    private String username;
    private int myChatPort = 8080;
    private String myAddress = "localhost:8080";
    private DiscoveryClient discoveryClient;

    // All open conversations
    private final ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private Conversation activeConversation;

   // UI components
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final List<Instant> messageKeys = new ArrayList<>();
    private Label activeConversationLabel;
    private ListView<String> messagesView;
    private TextField inputField;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void start(Stage primaryStage) {
        //Asking for username
        TextInputDialog userDialog = new TextInputDialog();
        userDialog.setTitle("Login");
        userDialog.setHeaderText("Enter your username");
        userDialog.setContentText("Name:");

        Optional<String> userResult = userDialog.showAndWait();
        username = userResult.orElse("anonymous").trim();
        if (username.isEmpty()) {
            username = "anonymous";
        }

        TextInputDialog portDialog = new TextInputDialog("8080");
        portDialog.setTitle("Chat Port");
        portDialog.setHeaderText("Enter the port your chat node listens on");
        portDialog.setContentText("Chat port:");

        String portStr = portDialog.showAndWait().orElse("8080").trim();
        try {
            myChatPort = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            myChatPort = 8080;
        }
        myAddress = "localhost:" + myChatPort; // local testing

        discoveryClient = new DiscoveryClient("http://localhost:8080/discovery");
        discoveryClient.register(username, myChatPort);

        //main layout
        BorderPane root = new BorderPane();

        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        BorderPane chatArea = buildChatArea();
        root.setCenter(chatArea);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("FunChat P2P");
        primaryStage.show();

        //Load saved conversations and histories for this user
        loadSavedConversations();
    }

    @Override
    public void stop() {
        // Save on exit
        saveConversations();
    }

    // Sidebar
    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(250);

        Label hello = new Label("Welcome, " + username + " 👋");
        hello.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Button newDmButton = new Button("New Direct Message");
        newDmButton.setMaxWidth(Double.MAX_VALUE);
        newDmButton.setOnAction(e -> createDirectMessageConversation());

        Button newGroupButton = new Button("New Group Chat");
        newGroupButton.setMaxWidth(Double.MAX_VALUE);
        newGroupButton.setOnAction(e -> createGroupConversation());

        Label convLabel = new Label("Conversations");
        convLabel.setPadding(new Insets(10, 0, 0, 0));

        ListView<Conversation> convoListView = new ListView<>(conversations);
        convoListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        convoListView.getSelectionModel().selectedItemProperty().addListener((obs, oldConv, newConv) -> {
            if (newConv != null) {
                selectConversation(newConv);
            }
        });

        VBox.setVgrow(convoListView, Priority.ALWAYS);

        sidebar.getChildren().addAll(hello, newDmButton, newGroupButton, convLabel, convoListView);
        return sidebar;
    }

    private BorderPane buildChatArea() {
        BorderPane chatArea = new BorderPane();
        chatArea.setPadding(new Insets(10));

        activeConversationLabel = new Label("No conversation selected");
        activeConversationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        chatArea.setTop(activeConversationLabel);
        BorderPane.setMargin(activeConversationLabel, new Insets(0, 0, 10, 0));

        messagesView = new ListView<>(messages);
        messagesView.setFocusTraversable(false);

        // Reactions via context menu
        messagesView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();

            ContextMenu menu = new ContextMenu();
            String[] emojis = {"👍", "❤️", "😂", "😮", "😢"}; //can maybe add more emotes later? 

            for (String emoji : emojis) {
                MenuItem item = new MenuItem("React " + emoji);
                item.setOnAction(e -> {
                    int index = cell.getIndex();
                    ChatClientService svc = getActiveService();
                    if (svc != null && index >= 0) {
                        svc.reactToMessageAtIndex(index, emoji);
                    }
                });
                menu.getItems().add(item);
            }

            cell.textProperty().bind(cell.itemProperty());

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                cell.setContextMenu(isNowEmpty ? null : menu);
            });

            return cell;
        });

        chatArea.setCenter(messagesView);
        BorderPane.setMargin(messagesView, new Insets(0, 0, 10, 0));

        inputField = new TextField();
        inputField.setPromptText("Type a message…");

        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> sendCurrentText());
        inputField.setOnAction(e -> sendCurrentText());

        HBox inputBar = new HBox(8, inputField, sendButton);
        inputBar.setPadding(new Insets(5, 0, 0, 0));
        HBox.setHgrow(inputField, Priority.ALWAYS);

        chatArea.setBottom(inputBar);

        return chatArea;
    }

     //create direct message by peer username, and resolve via discovery

    private void createDirectMessageConversation() {
        if (discoveryClient == null) {
            discoveryClient = new DiscoveryClient("http://localhost:8080/discovery"); //changed from 5050 to 8080 so it works
        }
        // Peer username(s)
        TextInputDialog peerNameDialog = new TextInputDialog("friend");
        peerNameDialog.setTitle("Direct Message Setup");
        peerNameDialog.setHeaderText("Peer username");
        peerNameDialog.setContentText("Their username (as registered in discovery):");
        String peerName = peerNameDialog.showAndWait().orElse("friend").trim();
        if (peerName.isEmpty()) {
            peerName = "friend";
        }
        // Look up peer address: "ip:port"
        String peerAddr = discoveryClient.search(peerName);
        if (peerAddr == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "User '" + peerName + "' not found in discovery server.\n" +
                    "Make sure their app is running and registered.");
            alert.setHeaderText("Peer not found");
            alert.showAndWait();
            return;
        }

        TreeMap<String, String> members = new TreeMap<>();
        members.put(username, myAddress);
        members.put(peerName, peerAddr);

        String title = "DM with " + peerName;

        createConversation(title, members);
    }


     //create group chat using usernames and resolve it with discovery

    private void createGroupConversation() {
        if (discoveryClient == null) {
            discoveryClient = new DiscoveryClient("http://localhost:8080/discovery");
        }

            //instructions...
        TextInputDialog groupDialog = new TextInputDialog(username + ",friend1,friend2");
        groupDialog.setTitle("Group Chat Setup");
        groupDialog.setHeaderText("Enter group member usernames (including yourself)");
        groupDialog.setContentText("Usernames (comma-separated):");

        Optional<String> groupResult = groupDialog.showAndWait();
        String groupConfig = groupResult.orElse("").trim();
        if (groupConfig.isEmpty()) {
            return;
        }

        TreeMap<String, String> members = new TreeMap<>();
        String[] names = groupConfig.split(",");

        for (String raw : names) {
            String name = raw.trim();
            if (name.isEmpty()) continue;

            String addr = discoveryClient.search(name);
            if (addr == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "User '" + name + "' not found in discovery server.\n" +
                        "They must have registered before joining this group.");
                alert.setHeaderText("Member not found");
                alert.showAndWait();
                return; 
            }
            members.put(name, addr);
        }

        if (!members.containsKey(username)) {
            members.put(username, myAddress);
        }

        String title = "Group Chat (" + members.keySet().size() + " members)";
        createConversation(title, members);
    }

    private void createConversation(String title, TreeMap<String, String> members) {
        // Create Conversation object
        Conversation conv = new Conversation(title, members);

        ChatClientService service = new ChatClientService(history -> updateFromHistoryForConversation(conv, history));
        conv.setService(service);
        service.connect(username, members, conv.getLastHistory());
        conversations.add(conv);
        selectConversation(conv);

        // Persist new conversation
        saveConversations();
    }

    //Conversation selection and UI updates
    private void selectConversation(Conversation conv) {
        this.activeConversation = conv;
        activeConversationLabel.setText(conv.getName());

        if (conv.getService() == null) {
            ChatClientService service = new ChatClientService(history -> updateFromHistoryForConversation(conv, history));
            conv.setService(service);

            service.connect(username, conv.getMembers(), conv.getLastHistory());
        }

        // Rendering the most recent history for a conversation
        MessageHistory history = conv.getLastHistory();
        if (history != null) {
            renderHistory(history);
        } else {
            messages.clear();
            messageKeys.clear();
        }
    }

    private ChatClientService getActiveService() {
        return (activeConversation != null) ? activeConversation.getService() : null;
    }

    private void sendCurrentText() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        ChatClientService svc = getActiveService();
        if (svc == null) { //make a conversation first
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please create or select a conversation first.");
            alert.setHeaderText("No conversation selected"); 
            alert.showAndWait();
            return;
        }

        svc.sendMessage(text);
        inputField.clear();
    }

    // Calling this by each ChatClientService whenever its MessageHistory changes.

    private void updateFromHistoryForConversation(Conversation conv, MessageHistory history) {
        //Copying history into conversation model
        conv.setLastHistory(new MessageHistory(history));

        saveConversations();

        // If this is the active conversation then it updates the UI. Have to do this so it renders properly
        if (conv == activeConversation) {
            renderHistory(history);
        }
    }

    private void renderHistory(MessageHistory history) {
        Platform.runLater(() -> {
            messages.clear();
            messageKeys.clear();

            for (Map.Entry<Instant, Message> entry : history.entrySet()) {
                Instant key = entry.getKey();
                Message m = entry.getValue();
                messageKeys.add(key);

                String ts = DateTimeFormatter.ISO_INSTANT.format(key);

                String user = m.getUsername();
                if (user == null || user.isBlank()) {
                    user = "???";
                }

                // Build emote reactions
                String reactionsStr = "";
                if (m.getReactions() != null && !m.getReactions().isEmpty()) {
                    StringBuilder sb = new StringBuilder(" [");
                    boolean first = true;
                    for (Map.Entry<String, String> r : m.getReactions().entrySet()) {
                        if (!first) sb.append("  ");
                        sb.append(r.getKey()).append(": ").append(r.getValue());
                        first = false;
                    }
                    sb.append("]");
                    reactionsStr = sb.toString();
                }

                String line = "[" + ts + "] " + user + ": " + m.getContents() + reactionsStr;
                messages.add(line);
            }

            if (activeConversation == null) {
                activeConversationLabel.setText("No conversation selected");
            }
        });
    }

    // Persistence helpers

    private Path getSaveFilePath() {
        String home = System.getProperty("user.home");
        Path dir = Paths.get(home, ".funchat");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir.resolve(username + "_chats.json");
    }

    private void saveConversations() {
        if (username == null || username.isBlank()) return;

        SavedState state = new SavedState();
        state.setUsername(username);
        List<SavedConversation> savedList = new ArrayList<>();

        for (Conversation conv : conversations) {
            SavedConversation sc = new SavedConversation();
            sc.setName(conv.getName());
            sc.setMembers(conv.getMembers());
            sc.setHistory(conv.getLastHistory());
            savedList.add(sc);
        }
        state.setConversations(savedList);

        Path savePath = getSaveFilePath();
        try (Writer w = Files.newBufferedWriter(savePath)) {
            MAPPER.writeValue(w, state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSavedConversations() {
        Path savePath = getSaveFilePath();
        if (!Files.exists(savePath)) {
            return;
        }

        try (Reader r = Files.newBufferedReader(savePath)) {
            SavedState state = MAPPER.readValue(r, SavedState.class);
            if (state.getConversations() != null) {
                for (SavedConversation sc : state.getConversations()) {
                    Conversation conv = new Conversation(sc.getName(), sc.getMembers());
                    if (sc.getHistory() != null) {
                        conv.setLastHistory(sc.getHistory());
                    }
                    conversations.add(conv);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Conversations
    private static class Conversation {
        private final String name;
        private final TreeMap<String, String> members;
        private ChatClientService service;
        private MessageHistory lastHistory = new MessageHistory();

        public Conversation(String name, TreeMap<String, String> members) {
            this.name = name;
            this.members = members;
        }

        public String getName() {
            return name;
        }

        public TreeMap<String, String> getMembers() {
            return members;
        }

        public ChatClientService getService() {
            return service;
        }

        public void setService(ChatClientService service) {
            this.service = service;
        }

        public MessageHistory getLastHistory() {
            return lastHistory;
        }

        public void setLastHistory(MessageHistory lastHistory) {
            this.lastHistory = lastHistory;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // JSON persistence models
    public static class SavedState {
        private String username;
        private List<SavedConversation> conversations;

        public SavedState() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<SavedConversation> getConversations() {
            return conversations;
        }

        public void setConversations(List<SavedConversation> conversations) {
            this.conversations = conversations;
        }
    }

    // saved convos 
    public static class SavedConversation {
        private String name;
        private TreeMap<String, String> members;
        private MessageHistory history;

        public SavedConversation() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TreeMap<String, String> getMembers() {
            return members;
        }

        public void setMembers(TreeMap<String, String> members) {
            this.members = members;
        }

        public MessageHistory getHistory() {
            return history;
        }

        public void setHistory(MessageHistory history) {
            this.history = history;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
