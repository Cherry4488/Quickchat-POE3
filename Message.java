package com.mycompany.progpoepart1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

/**
 * Represents one QuickChat message and keeps the arrays that track all messages.
 *
 * Attribution: reading and writing the JSON file with the Gson library follows the
 * Gson User Guide, https://github.com/google/gson/blob/main/UserGuide.md
 */
public class Message {

    // name of the JSON file that holds the stored messages
    public static final String JSON_FILE = "messages.json";
    private static final String DEFAULT_SENDER = "Unknown";

    // details of one message
    private String messageID;
    private int messageNumber;
    private String sender;
    private String recipient;
    private String messageText;
    private String messageHash;

    // arrays that keep track of messages while the program is running
    private static ArrayList<Message> sentMessages = new ArrayList<>();
    private static ArrayList<Message> disregardedMessages = new ArrayList<>();
    private static ArrayList<Message> storedMessages = new ArrayList<>();

    // parallel arrays: the hash at position i belongs to the ID at position i
    private static ArrayList<String> messageHashes = new ArrayList<>();
    private static ArrayList<String> messageIDs = new ArrayList<>();

    // constructor used in Part 2 - the ID and hash are created automatically
    public Message(int messageNumber, String recipient, String messageText) {
        this(DEFAULT_SENDER, messageNumber, recipient, messageText);
    }

    // constructor with a sender - the ID and hash are created automatically
    public Message(String sender, int messageNumber, String recipient, String messageText) {
        this(sender, createMessageID(), messageNumber, recipient, messageText);
    }

    // constructor with a known ID (used for the test data) - the hash is created automatically
    public Message(String sender, String messageID, int messageNumber, String recipient, String messageText) {
        this(sender, messageID, messageNumber, recipient, messageText,
                createMessageHash(messageID, messageNumber, messageText));
    }

    // constructor with a known ID and hash (used when a message is read from the JSON file)
    public Message(String sender, String messageID, int messageNumber, String recipient,
                   String messageText, String messageHash) {
        this.sender = sender;
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = messageHash;
    }

    // getters
    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    // creates a random ten digit message ID
    public static String createMessageID() {
        Random rand = new Random();
        String id = "";
        for (int i = 0; i < 10; i++) {
            id = id + rand.nextInt(10);
        }
        return id;
    }

    // checks that the message ID is not more than ten characters
    public static boolean checkMessageID(String id) {
        return id != null && id.length() <= 10;
    }

    // checks the recipient number - reuses the cell number check from Part 1 (Login class)
    public static String checkRecipientCell(String cell) {
        Login checker = new Login();
        checker.setCellPhoneNumber(cell);

        if (checker.checkCellPhoneNumber()) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    // checks that the message is not longer than 250 characters
    public static String checkMessageLength(String text) {
        if (text.length() <= 250) {
            return "Message ready to send.";
        } else {
            int extra = text.length() - 250;
            return "Message exceeds 250 characters by " + extra + "; please reduce the size.";
        }
    }

    // creates the hash e.g. 00:0:HITHANKS
    public static String createMessageHash(String id, int number, String text) {
        String firstTwo = id.length() >= 2 ? id.substring(0, 2) : id;

        // split the message into words
        String[] words = text.trim().split(" ");
        String firstWord = words[0];
        String lastWord = words[words.length - 1];

        // remove commas, question marks etc so only letters and numbers are left
        firstWord = firstWord.replaceAll("[^a-zA-Z0-9]", "");
        lastWord = lastWord.replaceAll("[^a-zA-Z0-9]", "");

        String hash = firstTwo + ":" + number + ":" + firstWord + lastWord;
        return hash.toUpperCase();
    }

    // returns the message depending on what the user chose
    // 1 = send, 2 = disregard, 3 = store
    public static String sentMessage(int choice) {
        if (choice == 1) {
            return "Message successfully sent.";
        } else if (choice == 2) {
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            return "Message successfully stored.";
        } else {
            return "Invalid option.";
        }
    }

    // ---------------------------------------------------------------
    // Part 3: populating the arrays
    // ---------------------------------------------------------------

    // adds a message's hash and ID to the parallel arrays
    private static void recordHashAndID(Message m) {
        messageHashes.add(m.getMessageHash());
        messageIDs.add(m.getMessageID());
    }

    // adds a message to the sent array
    public static void addToSent(Message m) {
        sentMessages.add(m);
        recordHashAndID(m);
    }

    // adds a message to the disregarded array
    public static void addToDisregarded(Message m) {
        disregardedMessages.add(m);
        recordHashAndID(m);
    }

    // adds a message to the stored array (memory only, the file is not changed)
    public static void addToStored(Message m) {
        storedMessages.add(m);
        recordHashAndID(m);
    }

    // stores a message in the array and saves all stored messages to the JSON file
    public static void storeMessage(Message m) {
        addToStored(m);
        saveStoredMessages();
    }

    // returns the text of every message in a list
    private static ArrayList<String> textsOf(ArrayList<Message> list) {
        ArrayList<String> texts = new ArrayList<>();
        for (Message m : list) {
            texts.add(m.getMessageText());
        }
        return texts;
    }

    public static ArrayList<String> getSentMessageTexts() {
        return textsOf(sentMessages);
    }

    public static ArrayList<String> getDisregardedMessageTexts() {
        return textsOf(disregardedMessages);
    }

    public static ArrayList<String> getStoredMessageTexts() {
        return textsOf(storedMessages);
    }

    public static ArrayList<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    public static ArrayList<String> getMessageIDs() {
        return new ArrayList<>(messageIDs);
    }

    // returns the total number of messages sent
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // ---------------------------------------------------------------
    // Part 3: Stored Messages menu features
    // ---------------------------------------------------------------

    // 4a: the sender and recipient of every stored message
    public static String displaySendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "No stored messages found.";
        }
        String all = "";
        for (Message m : storedMessages) {
            all = all + "Sender: " + m.getSender() + " | Recipient: " + m.getRecipient() + "\n";
        }
        return all;
    }

    // 4b: the longest stored message (null if there are no stored messages)
    public static Message getLongestStoredMessage() {
        Message longest = null;
        for (Message m : storedMessages) {
            if (longest == null || m.getMessageText().length() > longest.getMessageText().length()) {
                longest = m;
            }
        }
        return longest;
    }

    // 4c: finds a message by its ID (null if the ID is not in the messageIDs array)
    public static Message searchByMessageID(String id) {
        if (!messageIDs.contains(id)) {
            return null;
        }
        for (Message m : allMessages()) {
            if (m.getMessageID().equals(id)) {
                return m;
            }
        }
        return null;
    }

    // 4d: the text of every sent or stored message for one recipient
    public static ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();
        ArrayList<Message> sentAndStored = new ArrayList<>(sentMessages);
        sentAndStored.addAll(storedMessages);

        for (Message m : sentAndStored) {
            if (m.getRecipient().equals(recipient)) {
                found.add(m.getMessageText());
            }
        }
        return found;
    }

    // 4e: deletes a message using its hash. The hash and ID arrays are parallel,
    // so the position of the hash also gives us the ID of the message to remove.
    public static String deleteMessageByHash(String hash) {
        int position = messageHashes.indexOf(hash);
        if (position == -1) {
            return "Message hash not found.";
        }

        Message target = searchByMessageID(messageIDs.get(position));
        if (target != null) {
            sentMessages.remove(target);
            disregardedMessages.remove(target);
            storedMessages.remove(target);
        }

        messageHashes.remove(position);
        messageIDs.remove(position);

        if (target == null) {
            return "Message hash not found.";
        }
        return "Message: \"" + target.getMessageText() + "\" successfully deleted.";
    }

    // 4f: a report with the full details of all stored messages
    public static String printStoredMessages() {
        return buildReport(storedMessages);
    }

    // returns all the messages that were sent (report)
    public static String printMessages() {
        return buildReport(sentMessages);
    }

    // builds a report of the full details of every message in a list
    private static String buildReport(ArrayList<Message> list) {
        if (list.isEmpty()) {
            return "No messages to display.";
        }
        String all = "";
        for (Message m : list) {
            all = all + "Message ID: " + m.getMessageID() + "\n"
                    + "Message Hash: " + m.getMessageHash() + "\n"
                    + "Sender: " + m.getSender() + "\n"
                    + "Recipient: " + m.getRecipient() + "\n"
                    + "Message: " + m.getMessageText() + "\n\n";
        }
        return all;
    }

    // all messages in one list (sent, then stored, then disregarded)
    private static ArrayList<Message> allMessages() {
        ArrayList<Message> all = new ArrayList<>(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);
        return all;
    }

    // ---------------------------------------------------------------
    // Part 3: reading and writing the JSON file
    // ---------------------------------------------------------------

    // reads the JSON file into the stored messages array
    // returns how many messages were loaded (0 if the file does not exist yet)
    public static int loadStoredMessages(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return 0;
        }

        int loaded = 0;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonArray()) {
                return 0;
            }

            for (JsonElement element : root.getAsJsonArray()) {
                JsonObject object = element.getAsJsonObject();
                String id = readField(object, "messageID", "");
                String text = readField(object, "message", "");

                // skip incomplete entries and messages that are already loaded
                if (id.isEmpty() || text.isEmpty() || messageIDs.contains(id)) {
                    continue;
                }

                int number = storedMessages.size();
                String hash = readField(object, "messageHash", createMessageHash(id, number, text));
                String sender = readField(object, "sender", DEFAULT_SENDER);
                String recipient = readField(object, "recipient", "");

                addToStored(new Message(sender, id, number, recipient, text, hash));
                loaded++;
            }
        }
        return loaded;
    }

    // returns the text of a JSON field, or a default value if the field is missing
    private static String readField(JsonObject object, String name, String defaultValue) {
        if (object.has(name) && !object.get(name).isJsonNull()) {
            return object.get(name).getAsString();
        }
        return defaultValue;
    }

    // saves every stored message to the default JSON file
    public static void saveStoredMessages() {
        saveStoredMessages(JSON_FILE);
    }

    // saves every stored message to the given JSON file
    public static void saveStoredMessages(String filePath) {
        JsonArray array = new JsonArray();
        for (Message m : storedMessages) {
            JsonObject object = new JsonObject();
            object.addProperty("messageID", m.getMessageID());
            object.addProperty("messageHash", m.getMessageHash());
            object.addProperty("sender", m.getSender());
            object.addProperty("recipient", m.getRecipient());
            object.addProperty("message", m.getMessageText());
            array.add(object);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (Writer writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.out.println("Could not save the message: " + e.getMessage());
        }
    }

    // clears all the arrays (used by the unit tests)
    public static void clearMessages() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }
}
