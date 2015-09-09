package client;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class GUI implements Observer {
    private JFrame jf = new JFrame();
    private JPanel panel;
    private Client client;
    private JTextArea textArea;

    public GUI(final Client client) {
        this.client = client;

        MigLayout layout = new MigLayout(
                "fillx,wrap 2",
                "[fill, grow]10[right]",
                "10[top]");
        panel = new JPanel(layout);

        populateGui();

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setLayout(new BorderLayout());
        jf.setLocationRelativeTo(null);
        jf.add(panel);
        jf.setSize(600, 400);
        jf.setVisible(true);
        jf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    client.stop();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                super.windowClosing(e);
            }
        });
    }

    private void populateGui() {
        textArea = new JTextArea();
        final JTextField textField = new JTextField();
        JButton sendButton = new JButton();

        textArea.setEditable(false);
        sendButton.setText("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Sending '" + textField.getText() + "'");
                client.send(textField.getText());
                textField.setText("");
            }
        });

        panel.add(textArea, "w 100, h 300, span 2");
        panel.add(textField, "w 100");
        panel.add(sendButton);
    }

    @Override
    public void update(Observable o, Object arg) {
        textArea.append(arg + "\n");
    }
}
