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
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testMain() throws Exception {

    }

    @Test
    public void testSend() throws Exception {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        Client client = new Client();
        client.connect("localhost",9090);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        assertTrue("Client is connected", Server.clientHandlers.size() == 1);

        //assertEquals("HELLO", client.receive());
    }

    @Test
    public void testSendUserlist() throws Exception {

    }

    @Test
    public void testRemoveHandler() throws Exception {

    }
}