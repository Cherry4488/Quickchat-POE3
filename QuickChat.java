package com.mycompany.progpoepart1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class QuickChat {

    // keeps asking until the user types a whole number
    public static int readNumber(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = input.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    // runs the QuickChat menu (called after a successful login)
    public static void startChat(Scanner input, String sender) {
        System.out.println("\nWelcome to QuickChat.");
        loadAndDisplayStoredMessages();

        // user decides how many messages they want to enter
        int maxMessages = readNumber(input, "How many messages do you want to enter? ");
        while (maxMessages < 1) {
            maxMessages = readNumber(input, "Please enter at least 1: ");
        }
        int messagesEntered = 0;

        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            int option = readNumber(input, "Choose an option: ");

            if (option == 1) {
                if (messagesEntered >= maxMessages) {
                    System.out.println("You have already entered all " + maxMessages + " messages.");
                } else {
                    sendNewMessage(input, sender, messagesEntered);
                    messagesEntered++;

                    // once all messages are done, show the total
                    if (messagesEntered == maxMessages) {
                        System.out.println("\nTotal messages sent: " + Message.returnTotalMessages());
                    }
                }

            } else if (option == 2) {
                System.out.println(Message.printMessages());

            } else if (option == 3) {
                System.out.println("Total messages sent: " + Message.returnTotalMessages());
                System.out.println("Goodbye!");
                running = false;

            } else if (option == 4) {
                storedMessagesMenu(input);

            } else {
                System.out.println("Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // reads the JSON file into the stored messages array and shows it to the user
    private static void loadAndDisplayStoredMessages() {
        try {
            int loaded = Message.loadStoredMessages(Message.JSON_FILE);
            if (loaded > 0) {
                System.out.println(loaded + " stored message(s) loaded from " + Message.JSON_FILE + ":\n");
                System.out.println(Message.printStoredMessages());
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("The stored messages file could not be read: " + e.getMessage());
        }
    }

    // asks for a recipient and a message, then sends, disregards or stores it
    private static void sendNewMessage(Scanner input, String sender, int messageNumber) {
        String recipient = readRecipient(input);
        String text = readMessageText(input);

        // create the message (message number starts at 0)
        Message msg = new Message(sender, messageNumber, recipient, text);
        System.out.println("Message ID generated: " + msg.getMessageID());

        // ask what to do with the message
        System.out.println("\n1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int choice = readNumber(input, "Choose an option: ");
        while (choice < 1 || choice > 3) {
            choice = readNumber(input, "Please choose 1, 2 or 3: ");
        }

        System.out.println(Message.sentMessage(choice));

        if (choice == 1) {
            Message.addToSent(msg);
            // show the full details after sending
            System.out.println("\nMessage ID: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getMessageHash());
            System.out.println("Recipient: " + msg.getRecipient());
            System.out.println("Message: " + msg.getMessageText());
        } else if (choice == 2) {
            Message.addToDisregarded(msg);
            String del = input.nextLine().trim();
            if (del.equals("0")) {
                System.out.println("Message deleted.");
            }
        } else {
            Message.storeMessage(msg);
        }
    }

    // keeps asking until the recipient number is valid
    private static String readRecipient(Scanner input) {
        while (true) {
            System.out.print("Enter the recipient number: ");
            String recipient = input.nextLine().trim();
            String result = Message.checkRecipientCell(recipient);
            System.out.println(result);
            if (result.equals("Cell phone number successfully captured.")) {
                return recipient;
            }
        }
    }

    // keeps asking until the message is not empty and not longer than 250 characters
    private static String readMessageText(Scanner input) {
        while (true) {
            System.out.print("Enter your message: ");
            String text = input.nextLine();
            String result = Message.checkMessageLength(text);
            System.out.println(result);
            if (text.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
            } else if (result.equals("Message ready to send.")) {
                return text;
            }
        }
    }

    // the fourth main menu option: Stored Messages
    private static void storedMessagesMenu(Scanner input) {
        System.out.println("\n--- STORED MESSAGES ---");
        System.out.println("a) Display the sender and recipient of all stored messages");
        System.out.println("b) Display the longest stored message");
        System.out.println("c) Search for a message ID");
        System.out.println("d) Search for all messages sent or stored for a particular recipient");
        System.out.println("e) Delete a message using the message hash");
        System.out.println("f) Display a report of all stored messages");
        System.out.print("Choose an option: ");

        String option = input.nextLine().trim().toLowerCase();

        if (option.equals("a")) {
            System.out.println(Message.displaySendersAndRecipients());
        } else if (option.equals("b")) {
            displayLongestMessage();
        } else if (option.equals("c")) {
            searchForMessageID(input);
        } else if (option.equals("d")) {
            searchForRecipient(input);
        } else if (option.equals("e")) {
            deleteUsingHash(input);
        } else if (option.equals("f")) {
            System.out.println(Message.printStoredMessages());
        } else {
            System.out.println("Please choose a, b, c, d, e or f.");
        }
    }

    private static void displayLongestMessage() {
        Message longest = Message.getLongestStoredMessage();
        if (longest == null) {
            System.out.println("No stored messages found.");
        } else {
            System.out.println("Longest stored message:");
            System.out.println("Recipient: " + longest.getRecipient());
            System.out.println("Message: " + longest.getMessageText());
        }
    }

    private static void searchForMessageID(Scanner input) {
        System.out.print("Enter the message ID: ");
        Message found = Message.searchByMessageID(input.nextLine().trim());
        if (found == null) {
            System.out.println("Message ID not found.");
        } else {
            System.out.println("Recipient: " + found.getRecipient());
            System.out.println("Message: " + found.getMessageText());
        }
    }

    private static void searchForRecipient(Scanner input) {
        System.out.print("Enter the recipient number: ");
        ArrayList<String> found = Message.searchByRecipient(input.nextLine().trim());
        if (found.isEmpty()) {
            System.out.println("No messages found for that recipient.");
        } else {
            for (String text : found) {
                System.out.println("\"" + text + "\"");
            }
        }
    }

    // deletes the message and keeps the JSON file up to date
    private static void deleteUsingHash(Scanner input) {
        System.out.print("Enter the message hash: ");
        System.out.println(Message.deleteMessageByHash(input.nextLine().trim()));
        Message.saveStoredMessages();
    }
}
