package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.ProtocolStrings;

public class Client extends Observable {
    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;

    public void connect(String address, int port) throws UnknownHostException, IOException
    {
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
    }

    public void send(String msg)
    {
        output.println(msg);
    }

    public void stop() throws IOException{
        output.println(ProtocolStrings.STOP);
    }

    public static void main(String[] args) {
        int port = 9090;
        //String ip = "honason.cloudapp.net";
        String ip = "localhost";
        if(args.length == 2){
            port = Integer.parseInt(args[0]);
            ip = args[1];
        }
        try {
            Client clientInstance = new Client();
            GUI gui = new GUI(clientInstance);
            clientInstance.addObserver(gui);
            clientInstance.connect(ip, port);
            clientInstance.runChecker();
            gui.init();

            //System.in.read();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void runChecker() {
        Thread t = new Thread(new Runnable() { public void run() {
            while (true) {
                String msg = input.nextLine();
                setChanged();
                notifyObservers(msg);

                if(msg.equals(ProtocolStrings.STOP)){
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }});

        t.start();
    }
}
