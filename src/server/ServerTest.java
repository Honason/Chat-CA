package server;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import client.Client;

import static org.junit.Assert.*;

public class ServerTest {

    @BeforeClass
    public static void setUpClass() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                Server.main(null);
            }
        }).start();

        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testConnect() throws Exception {
        Client client = new Client();
        client.connect("localhost",9090);
        client.runChecker();
        Client client2 = new Client();
        client2.connect("localhost",9090);
        client2.runChecker();

        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        assertTrue("Clients are connected", Server.clientHandlers.size() == 2);
        //assertEquals("HELLO", client.receive());
        client.send("STOP#");
        client2.send("STOP#");
    }

    @Test
    public void testSendUserlist() throws Exception {
        Client client = new Client();
        client.connect("localhost", 9090);
        client.runChecker();

        client.send("USER#Jensa");

        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        int msgFound = 0;
        for (String message : client.getMessageList()) {
            if (message.equals("MSG#Chat server#Welcome Jensa")) {
                msgFound++;
            } else if (message.contains("Jensa") && message.contains("USERLIST#")) {
                msgFound++;
            }
        }
        assertTrue(msgFound == 2);
        client.send("STOP#");
    }

    @Test
    public void testSendMessage() throws Exception {
        Client client = new Client();
        client.connect("localhost", 9090);
        client.runChecker();

        Client client2 = new Client();
        client2.connect("localhost", 9090);
        client2.runChecker();

        client.send("USER#user1");
        client2.send("USER#user2");

        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        client.send("MSG#user2#Hi User 2");
        client2.send("MSG#user1#Hi User 1");

        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        int msgFound = 0;
        for (String message : client.getMessageList()) {
            if (message.equals("MSG#user2#Hi User 1")) {
                msgFound++;
            }
        }
        for (String message : client2.getMessageList()) {
            if (message.equals("MSG#user1#Hi User 2")) {
                msgFound++;
            }
        }

        assertTrue("Message received", msgFound == 2);

        client.send("STOP#");
        client2.send("STOP#");
    }
}