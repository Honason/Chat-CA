package server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.ProtocolStrings;
import utils.Utils;

public class Server {
    private static final Properties properties = Utils.initProperties("server.properties");
    private static ServerSocket serverSocket;
    private static boolean keepRunning = true;
    public static BlockingQueue<ClientHandler> clientHandlers = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) {
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, Server.class.getName());

        try {
            new Server().runServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Utils.closeLogger(Server.class.getName());
    }

    private void runServer() throws InterruptedException {
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");

        Logger.getLogger(Server.class.getName()).log(Level.INFO, "Sever started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(Server.class.getName()).log(Level.INFO, "Connected to a client");
                ClientHandler handler = new ClientHandler(socket);
                clientHandlers.add(handler);
                handler.start();
            } while (keepRunning);
        }
        catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void send(String msg, String sender) {
        // Message
        if (msg.indexOf(ProtocolStrings.MESSAGE) == 0) {
            String[] messageArray = msg.split("#");
            String message = messageArray[messageArray.length-1];

            if (messageArray[1].equals("*")) {
                for (ClientHandler handler : clientHandlers) {
                    handler.send(ProtocolStrings.MESSAGE + sender + "#" + message);
                }
            } else {
                String[] recipients = messageArray[1].split(",");
                for (String recipient : recipients){
                    for (ClientHandler handler : clientHandlers) {
                        if (recipient.equals(handler.clientName)) {
                            handler.send(ProtocolStrings.MESSAGE + sender + "#" + message);
                        }
                    }
                }
            }
        }
    }

    public static void sendUserlist() {
        String userList = "";

        if (clientHandlers.size() > 0) {
            for (ClientHandler handler : clientHandlers) {
                userList += handler.clientName + ",";
            }
            userList = userList.substring(0,userList.length()-1);

            for (ClientHandler handler : clientHandlers) {
                handler.send(ProtocolStrings.USERLIST + userList);
            }
        }
    }

    public static void removeHandler(ClientHandler ch) {
        clientHandlers.remove(ch);
    }
}
