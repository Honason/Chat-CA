package server;

import protocol.ProtocolStrings;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {

    private Socket socket;
    private Scanner input;
    private PrintWriter writer;
    public String clientName;

    public ClientHandler(Socket socket) throws IOException, InterruptedException {
        this.socket = socket;
        input = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void run() {
        String message = input.nextLine();
        Logger.getLogger(Server.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ",message));

        while (!message.equals(ProtocolStrings.STOP)) {
            if (message.indexOf(ProtocolStrings.HANDSHAKE) == 0){
                String[] name = message.split("#");
                clientName = name[1];
                Server.send(ProtocolStrings.MESSAGE + clientName + "#Welcome " + name[1], "Chat server");
                Server.sendUserlist();
            } else if (clientName != null && message.indexOf(ProtocolStrings.MESSAGE) == 0){
                Server.send(message, clientName);
            }

            try {
                message = input.nextLine();
            } catch (NoSuchElementException e) {
                break;
            }
        }

        writer.println(ProtocolStrings.STOP);//Echo the stop message back to the client for a nice closedown
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.getLogger(Server.class.getName()).log(Level.INFO, "Closed a Connection");
        Server.removeHandler(this);
        Server.sendUserlist();
    }

    public void send(String message) {
        writer.println(message);
        Logger.getLogger(Server.class.getName()).log(Level.INFO, String.format("Sent the message: %1$S ",message));
    }
}
