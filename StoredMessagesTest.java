package com.mycompany.progpoepart1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Part 3 unit tests. They use the test data from the assignment brief.
 * The tests only change the arrays in memory, so messages.json is never touched.
 */
public class StoredMessagesTest {

    private static final String LATE_MESSAGE = "Where are you? You are late! I have asked you to be on time.";

    @TempDir
    Path tempFolder;

    @BeforeEach
    public void setUp() {
        Message.clearMessages();

        // Test data messages 1 to 5 (the message ID of message 4 is its number, as in the brief)
        Message.addToSent(new Message("Developer", "1000000001", 0, "+27834557896", "Did you get the cake?"));
        Message.addToStored(new Message("Developer", "1000000002", 1, "+27838884567", LATE_MESSAGE));
        Message.addToDisregarded(new Message("Developer", "1000000003", 2, "+27834484567", "Yohoooo, I am at your gate."));
        Message.addToSent(new Message("Developer", "0838884567", 3, "0838884567", "It is dinner time!"));
        Message.addToStored(new Message("Developer", "1000000005", 4, "+27838884567", "Ok, I am leaving without you."));
    }

    // ---- arrays correctly populated ----
    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList("Did you get the cake?", "It is dinner time!");
        assertEquals(expected, Message.getSentMessageTexts());
    }

    @Test
    public void testDisregardedMessagesArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList("Yohoooo, I am at your gate.");
        assertEquals(expected, Message.getDisregardedMessageTexts());
    }

    @Test
    public void testStoredMessagesArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList(LATE_MESSAGE, "Ok, I am leaving without you.");
        assertEquals(expected, Message.getStoredMessageTexts());
    }

    @Test
    public void testMessageIDArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList("1000000001", "1000000002", "1000000003", "0838884567", "1000000005");
        assertEquals(expected, Message.getMessageIDs());
    }

    @Test
    public void testMessageHashArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList("10:0:DIDCAKE", "10:1:WHERETIME", "10:2:YOHOOOOGATE",
                "08:3:ITTIME", "10:4:OKYOU");
        assertEquals(expected, Message.getMessageHashes());
    }

    // ---- longest message ----
    @Test
    public void testDisplayLongestMessage() {
        assertEquals(LATE_MESSAGE, Message.getLongestStoredMessage().getMessageText());
    }

    // ---- search for a message ID ----
    @Test
    public void testSearchForMessageID() {
        Message found = Message.searchByMessageID("0838884567");
        assertNotNull(found);
        assertEquals("It is dinner time!", found.getMessageText());
    }

    @Test
    public void testSearchForMessageIDNotFound() {
        assertNull(Message.searchByMessageID("9999999999"));
    }

    // ---- search for a recipient ----
    @Test
    public void testSearchMessagesForRecipient() {
        List<String> expected = Arrays.asList(LATE_MESSAGE, "Ok, I am leaving without you.");
        assertEquals(expected, Message.searchByRecipient("+27838884567"));
    }

    // ---- delete using the message hash ----
    @Test
    public void testDeleteMessageUsingHash() {
        String hash = Message.getMessageHashes().get(1); // Test Message 2
        String expected = "Message: \"" + LATE_MESSAGE + "\" successfully deleted.";
        assertEquals(expected, Message.deleteMessageByHash(hash));
    }

    @Test
    public void testDeleteRemovesMessageFromAllArrays() {
        Message.deleteMessageByHash("10:1:WHERETIME");
        assertEquals(Arrays.asList("Ok, I am leaving without you."), Message.getStoredMessageTexts());
        assertEquals(4, Message.getMessageHashes().size());
        assertEquals(4, Message.getMessageIDs().size());
    }

    @Test
    public void testDeleteHashNotFound() {
        assertEquals("Message hash not found.", Message.deleteMessageByHash("NO:0:HASH"));
    }

    // ---- display report ----
    @Test
    public void testDisplayReportOfSentMessages() {
        String expected =
                "Message ID: 1000000001\n"
                + "Message Hash: 10:0:DIDCAKE\n"
                + "Sender: Developer\n"
                + "Recipient: +27834557896\n"
                + "Message: Did you get the cake?\n\n"
                + "Message ID: 0838884567\n"
                + "Message Hash: 08:3:ITTIME\n"
                + "Sender: Developer\n"
                + "Recipient: 0838884567\n"
                + "Message: It is dinner time!\n\n";
        assertEquals(expected, Message.printMessages());
    }

    // ---- reading the JSON file into the array ----
    @Test
    public void testReadJsonFileIntoArray() throws IOException {
        Message.clearMessages();
        Path file = tempFolder.resolve("messages.json");
        String json = "[\n"
                + "  {\"messageID\": \"1000000002\", \"messageHash\": \"10:1:WHERETIME\","
                + " \"sender\": \"Developer\", \"recipient\": \"+27838884567\", \"message\": \"" + LATE_MESSAGE + "\"},\n"
                + "  {\"messageID\": \"1000000005\", \"messageHash\": \"10:4:OKYOU\","
                + " \"recipient\": \"+27838884567\", \"message\": \"Ok, I am leaving without you.\"}\n"
                + "]";
        Files.write(file, json.getBytes(StandardCharsets.UTF_8));

        assertEquals(2, Message.loadStoredMessages(file.toString()));
        assertEquals(Arrays.asList(LATE_MESSAGE, "Ok, I am leaving without you."), Message.getStoredMessageTexts());
        assertEquals(Arrays.asList("10:1:WHERETIME", "10:4:OKYOU"), Message.getMessageHashes());
        assertEquals("Unknown", Message.searchByMessageID("1000000005").getSender());
    }

    @Test
    public void testSaveThenReadJsonFile() throws IOException {
        String file = tempFolder.resolve("saved.json").toString();
        Message.saveStoredMessages(file);

        List<String> before = Message.getStoredMessageTexts();
        Message.clearMessages();

        assertEquals(2, Message.loadStoredMessages(file));
        assertEquals(before, Message.getStoredMessageTexts());
    }

    @Test
    public void testReadJsonFileThatDoesNotExist() throws IOException {
        Message.clearMessages();
        assertEquals(0, Message.loadStoredMessages(tempFolder.resolve("missing.json").toString()));
    }
}
