package client;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import javax.swing.text.html.HTMLDocument;
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
import protocol.ProtocolStrings;

public class GUI implements Observer {
    private JFrame jf = new JFrame();
    private JPanel panel;
    private Client client;
    private JTextPane textPane;
    private JScrollPane scrollPane;
    private JList userList;
    private DefaultListModel userListData;
    private JButton whisperBtn;
    private ProtocolStrings ps;
    private ArrayList<String> selectedPeople = new ArrayList<>();

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
        textPane = new JTextPane();
        scrollPane = new JScrollPane();
        userListData = new DefaultListModel();
        userList = new JList(userListData);
        final JTextField textField = new JTextField();
        scrollPane.getViewport().add(textPane);
        textPane.setContentType("text/html");
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setEditable(false);
        // userList.setEditable(false);
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userList.getSelectedIndex() == 0)
                    send(textField.getText());
                else {
                    String recepients = "";
                    for (String recepient : (ArrayList<String>) userList.getSelectedValuesList()) {
                        recepients += recepient + ",";
                    }
                    if (recepients.length() > 0)
                        recepients = recepients.substring(0, recepients.length() - 1);
                    if (!textField.getText().equals(""))
                        send("/w " + recepients + "/" + textField.getText());
                }
                textField.setText("");
            }
        });

        userList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (index0 == index1) { // Single click
                    if (userList.isSelectedIndex(index0)) {
                        if (userList.getSelectedIndices().length == 1) { // if deselect all, select first index (All)
                            userList.removeSelectionInterval(0, userListData.size() - 1);
                            userList.addSelectionInterval(0, 0);
                        } else
                            userList.removeSelectionInterval(index0, index1);
                    } else {
                        if (index0 == 0 && index1 == 0) {
                            userList.removeSelectionInterval(0, userListData.size() - 1);
                        } else
                            userList.removeSelectionInterval(0, 0);
                        userList.addSelectionInterval(index0, index1);
                    }
                } else {
                    if (index0 == 0 || index1 == 0) {
                        userList.removeSelectionInterval(0, userListData.size() - 1);
                        userList.addSelectionInterval(0, 0);
                    } else if (userList.isSelectedIndex(index0)) {
                        userList.addSelectionInterval(index0, index1);
                    } else {
                        userList.removeSelectionInterval(index0, index1);
                    }
                }


            }
        });

        panel.add(scrollPane, "w 100, growy");
        panel.add(userList, "w 100, h 300");
        panel.add(textField, "w 100, h 50, span 2");
    }

    @Override
    public void update(Observable o, Object arg) {
        String msg = arg.toString();
        System.out.println("received: " + msg);
        if(msg.indexOf(ps.USERLIST) == 0) {
            selectedPeople.clear();
            for (int ind : userList.getSelectedIndices()) {
                selectedPeople.add((String) userListData.get(ind));
            }
            userListData.removeAllElements();
            userListData.addElement("All");
            for (String u : msg.substring(ps.USERLIST.length()).split(",")) {
                userListData.addElement(u);
                for (String person : selectedPeople) {
                    if(u.equals(person))
                        userList.addSelectionInterval(userListData.size() - 1, userListData.size() - 1);
                }
            }
            if(selectedPeople.size() == 0)
                userList.addSelectionInterval(0,0);
        } else if(msg.indexOf(ps.MESSAGE) == 0) {
            Calendar rightNow = Calendar.getInstance();
            String time = rightNow.get(Calendar.HOUR_OF_DAY) + ":" + rightNow.get(Calendar.MINUTE);

            String sender = msg.split("#")[1];
            msg = msg.substring(ps.MESSAGE.length() + sender.length() + 1);
            msg = kappafy(msg);
            try {
                HTMLDocument doc=(HTMLDocument) textPane.getStyledDocument();
                doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()),
                        "<font style='font-size:11px'><font style='color:gray'>" + time + " " + kappafy(sender) + "</font>: " + msg + "</font><br>");

            } catch(Exception e){}

            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue( vertical.getMaximum() );
        }
    }

    private void send(String msg) {
        if(msg.indexOf("/w ") == 0) {
            String receivers = msg.substring(3);receivers = receivers.substring(0, receivers.indexOf("/"));
            msg = msg.substring(3);msg = msg.substring(msg.indexOf("/") + 1);
            update(null, "MSG#Whisper to " + receivers + "#" + msg);
            msg = "MSG#" + receivers + "#" + msg;
        } else if(msg.indexOf("USER#") == 0 || msg.indexOf("STOP#") == 0) {
            msg = msg;
        } else {
            update(null, "MSG#You#" + msg);
            msg = "MSG#*#" + msg;
        }
        client.send(msg);
    }

    public void init() {
        final JTextField setNameField   = new JTextField(18);
        setNameField.setPreferredSize(new Dimension(200, 30));
        setNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send("USER#" + setNameField.getText());

                Window w = SwingUtilities.getWindowAncestor(setNameField);
                if (w != null) {
                  w.setVisible(false);
              }
          }
        });
        JPanel pairPanel = new JPanel();
        pairPanel.setLayout(new BorderLayout());

        JPanel north = new JPanel();
        north.add(setNameField);
        pairPanel.add(north, BorderLayout.NORTH);


        int clicked = JOptionPane.showOptionDialog(jf, pairPanel, 
            "Set name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);

    }


    public String kappafy(String msg) {
        String[][] kappas = new String[][]{
            {"4Head","https://static-cdn.jtvnw.net/emoticons/v1/354/1.0"},
            {"ANELE","https://static-cdn.jtvnw.net/emoticons/v1/3792/1.0"},
            {"ArgieB8","https://static-cdn.jtvnw.net/emoticons/v1/51838/1.0"},
            {"ArsonNoSexy","https://static-cdn.jtvnw.net/emoticons/v1/50/1.0"},
            {"AsianGlow","https://static-cdn.jtvnw.net/emoticons/v1/74/1.0"},
            {"AtGL","https://static-cdn.jtvnw.net/emoticons/v1/9809/1.0"},
            {"AthenaPMS","https://static-cdn.jtvnw.net/emoticons/v1/32035/1.0"},
            {"AtIvy","https://static-cdn.jtvnw.net/emoticons/v1/9800/1.0"},
            {"AtWW","https://static-cdn.jtvnw.net/emoticons/v1/9801/1.0"},
            {"BabyRage","https://static-cdn.jtvnw.net/emoticons/v1/22639/1.0"},
            {"BatChest","https://static-cdn.jtvnw.net/emoticons/v1/1905/1.0"},
            {"BCWarrior","https://static-cdn.jtvnw.net/emoticons/v1/30/1.0"},
            {"BibleThump","https://static-cdn.jtvnw.net/emoticons/v1/86/1.0"},
            {"BigBrother","https://static-cdn.jtvnw.net/emoticons/v1/1904/1.0"},
            {"BionicBunion","https://static-cdn.jtvnw.net/emoticons/v1/24/1.0"},
            {"BlargNaut","https://static-cdn.jtvnw.net/emoticons/v1/38/1.0"},
            {"bleedPurple","https://static-cdn.jtvnw.net/emoticons/v1/62835/1.0"},
            {"BloodTrail","https://static-cdn.jtvnw.net/emoticons/v1/69/1.0"},
            {"BORT","https://static-cdn.jtvnw.net/emoticons/v1/243/1.0"},
            {"BrainSlug","https://static-cdn.jtvnw.net/emoticons/v1/881/1.0"},
            {"BrokeBack","https://static-cdn.jtvnw.net/emoticons/v1/4057/1.0"},
            {"BuddhaBar","https://static-cdn.jtvnw.net/emoticons/v1/27602/1.0"},
            {"CoolCat","https://static-cdn.jtvnw.net/emoticons/v1/58127/1.0"},
            {"CorgiDerp","https://static-cdn.jtvnw.net/emoticons/v1/49106/1.0"},
            {"CougarHunt","https://static-cdn.jtvnw.net/emoticons/v1/21/1.0"},
            {"DAESuppy","https://static-cdn.jtvnw.net/emoticons/v1/973/1.0"},
            {"DansGame","https://static-cdn.jtvnw.net/emoticons/v1/33/1.0"},
            {"DatHass","https://static-cdn.jtvnw.net/emoticons/v1/20225/1.0"},
            {"DatSheffy","https://static-cdn.jtvnw.net/emoticons/v1/170/1.0"},
            {"DBstyle","https://static-cdn.jtvnw.net/emoticons/v1/73/1.0"},
            {"deExcite","https://static-cdn.jtvnw.net/emoticons/v1/46249/1.0"},
            {"deIlluminati","https://static-cdn.jtvnw.net/emoticons/v1/46248/1.0"},
            {"DendiFace","https://static-cdn.jtvnw.net/emoticons/v1/58135/1.0"},
            {"DogFace","https://static-cdn.jtvnw.net/emoticons/v1/1903/1.0"},
            {"DOOMGuy","https://static-cdn.jtvnw.net/emoticons/v1/54089/1.0"},
            {"duDudu","https://static-cdn.jtvnw.net/emoticons/v1/62834/1.0"},
            {"EagleEye","https://static-cdn.jtvnw.net/emoticons/v1/20/1.0"},
            {"EleGiggle","https://static-cdn.jtvnw.net/emoticons/v1/4339/1.0"},
            {"EvilFetus","https://static-cdn.jtvnw.net/emoticons/v1/72/1.0"},
            {"FailFish","https://static-cdn.jtvnw.net/emoticons/v1/360/1.0"},
            {"FPSMarksman","https://static-cdn.jtvnw.net/emoticons/v1/42/1.0"},
            {"FrankerZ","https://static-cdn.jtvnw.net/emoticons/v1/65/1.0"},
            {"FreakinStinkin","https://static-cdn.jtvnw.net/emoticons/v1/39/1.0"},
            {"FUNgineer","https://static-cdn.jtvnw.net/emoticons/v1/244/1.0"},
            {"FunRun","https://static-cdn.jtvnw.net/emoticons/v1/48/1.0"},
            {"FuzzyOtterOO","https://static-cdn.jtvnw.net/emoticons/v1/168/1.0"},
            {"GasJoker","https://static-cdn.jtvnw.net/emoticons/v1/9802/1.0"},
            {"GingerPower","https://static-cdn.jtvnw.net/emoticons/v1/32/1.0"},
            {"GrammarKing","https://static-cdn.jtvnw.net/emoticons/v1/3632/1.0"},
            {"HassanChop","https://static-cdn.jtvnw.net/emoticons/v1/68/1.0"},
            {"HeyGuys","https://static-cdn.jtvnw.net/emoticons/v1/30259/1.0"},
            {"HotPokket","https://static-cdn.jtvnw.net/emoticons/v1/357/1.0"},
            {"HumbleLife","https://static-cdn.jtvnw.net/emoticons/v1/46881/1.0"},
            {"ItsBoshyTime","https://static-cdn.jtvnw.net/emoticons/v1/169/1.0"},
            {"Jebaited","https://static-cdn.jtvnw.net/emoticons/v1/90/1.0"},
            {"JKanStyle","https://static-cdn.jtvnw.net/emoticons/v1/15/1.0"},
            {"JonCarnage","https://static-cdn.jtvnw.net/emoticons/v1/26/1.0"},
            {"KAPOW","https://static-cdn.jtvnw.net/emoticons/v1/9803/1.0"},
            {"Kappa","https://static-cdn.jtvnw.net/emoticons/v1/25/1.0"},
            {"KappaPride","https://static-cdn.jtvnw.net/emoticons/v1/55338/1.0"},
            {"Keepo","https://static-cdn.jtvnw.net/emoticons/v1/1902/1.0"},
            {"KevinTurtle","https://static-cdn.jtvnw.net/emoticons/v1/40/1.0"},
            {"Kippa","https://static-cdn.jtvnw.net/emoticons/v1/1901/1.0"},
            {"Kreygasm","https://static-cdn.jtvnw.net/emoticons/v1/41/1.0"},
            {"KZskull","https://static-cdn.jtvnw.net/emoticons/v1/5253/1.0"},
            {"Mau5","https://static-cdn.jtvnw.net/emoticons/v1/30134/1.0"},
            {"mcaT","https://static-cdn.jtvnw.net/emoticons/v1/35063/1.0"},
            {"MechaSupes","https://static-cdn.jtvnw.net/emoticons/v1/9804/1.0"},
            {"MrDestructoid","https://static-cdn.jtvnw.net/emoticons/v1/28/1.0"},
            {"MVGame","https://static-cdn.jtvnw.net/emoticons/v1/29/1.0"},
            {"NightBat","https://static-cdn.jtvnw.net/emoticons/v1/9805/1.0"},
            {"NinjaTroll","https://static-cdn.jtvnw.net/emoticons/v1/45/1.0"},
            {"NoNoSpot","https://static-cdn.jtvnw.net/emoticons/v1/44/1.0"},
            {"NotATK","https://static-cdn.jtvnw.net/emoticons/v1/34875/1.0"},
            {"NotLikeThis","https://static-cdn.jtvnw.net/emoticons/v1/58765/1.0"},
            {"OMGScoots","https://static-cdn.jtvnw.net/emoticons/v1/91/1.0"},
            {"OneHand","https://static-cdn.jtvnw.net/emoticons/v1/66/1.0"},
            {"OpieOP","https://static-cdn.jtvnw.net/emoticons/v1/356/1.0"},
            {"OptimizePrime","https://static-cdn.jtvnw.net/emoticons/v1/16/1.0"},
            {"OSbeaver","https://static-cdn.jtvnw.net/emoticons/v1/47005/1.0"},
            {"OSbury","https://static-cdn.jtvnw.net/emoticons/v1/47420/1.0"},
            {"OSdeo","https://static-cdn.jtvnw.net/emoticons/v1/47007/1.0"},
            {"OSfrog","https://static-cdn.jtvnw.net/emoticons/v1/47008/1.0"},
            {"OSkomodo","https://static-cdn.jtvnw.net/emoticons/v1/47010/1.0"},
            {"OSrob","https://static-cdn.jtvnw.net/emoticons/v1/47302/1.0"},
            {"OSsloth","https://static-cdn.jtvnw.net/emoticons/v1/47011/1.0"},
            {"panicBasket","https://static-cdn.jtvnw.net/emoticons/v1/22998/1.0"},
            {"PanicVis","https://static-cdn.jtvnw.net/emoticons/v1/3668/1.0"},
            {"PazPazowitz","https://static-cdn.jtvnw.net/emoticons/v1/19/1.0"},
            {"PeoplesChamp","https://static-cdn.jtvnw.net/emoticons/v1/3412/1.0"},
            {"PermaSmug","https://static-cdn.jtvnw.net/emoticons/v1/27509/1.0"},
            {"PicoMause","https://static-cdn.jtvnw.net/emoticons/v1/27/1.0"},
            {"PipeHype","https://static-cdn.jtvnw.net/emoticons/v1/4240/1.0"},
            {"PJHarley","https://static-cdn.jtvnw.net/emoticons/v1/9808/1.0"},
            {"PJSalt","https://static-cdn.jtvnw.net/emoticons/v1/36/1.0"},
            {"PMSTwin","https://static-cdn.jtvnw.net/emoticons/v1/92/1.0"},
            {"PogChamp","https://static-cdn.jtvnw.net/emoticons/v1/88/1.0"},
            {"Poooound","https://static-cdn.jtvnw.net/emoticons/v1/358/1.0"},
            {"PraiseIt","https://static-cdn.jtvnw.net/emoticons/v1/38586/1.0"},
            {"PRChase","https://static-cdn.jtvnw.net/emoticons/v1/28328/1.0"},
            {"PunchTrees","https://static-cdn.jtvnw.net/emoticons/v1/47/1.0"},
            {"PuppeyFace","https://static-cdn.jtvnw.net/emoticons/v1/58136/1.0"},
            {"RaccAttack","https://static-cdn.jtvnw.net/emoticons/v1/27679/1.0"},
            {"RalpherZ","https://static-cdn.jtvnw.net/emoticons/v1/1900/1.0"},
            {"RedCoat","https://static-cdn.jtvnw.net/emoticons/v1/22/1.0"},
            {"ResidentSleeper","https://static-cdn.jtvnw.net/emoticons/v1/245/1.0"},
            {"riPepperonis","https://static-cdn.jtvnw.net/emoticons/v1/62833/1.0"},
            {"RitzMitz","https://static-cdn.jtvnw.net/emoticons/v1/4338/1.0"},
            {"RuleFive","https://static-cdn.jtvnw.net/emoticons/v1/361/1.0"},
            {"SeemsGood","https://static-cdn.jtvnw.net/emoticons/v1/64138/1.0"},
            {"ShadyLulu","https://static-cdn.jtvnw.net/emoticons/v1/52492/1.0"},
            {"Shazam","https://static-cdn.jtvnw.net/emoticons/v1/9807/1.0"},
            {"shazamicon","https://static-cdn.jtvnw.net/emoticons/v1/9806/1.0"},
            {"ShazBotstix","https://static-cdn.jtvnw.net/emoticons/v1/87/1.0"},
            {"ShibeZ","https://static-cdn.jtvnw.net/emoticons/v1/27903/1.0"},
            {"SMOrc","https://static-cdn.jtvnw.net/emoticons/v1/52/1.0"},
            {"SMSkull","https://static-cdn.jtvnw.net/emoticons/v1/51/1.0"},
            {"SoBayed","https://static-cdn.jtvnw.net/emoticons/v1/1906/1.0"},
            {"SoonerLater","https://static-cdn.jtvnw.net/emoticons/v1/355/1.0"},
            {"SriHead","https://static-cdn.jtvnw.net/emoticons/v1/14706/1.0"},
            {"SSSsss","https://static-cdn.jtvnw.net/emoticons/v1/46/1.0"},
            {"StoneLightning","https://static-cdn.jtvnw.net/emoticons/v1/17/1.0"},
            {"StrawBeary","https://static-cdn.jtvnw.net/emoticons/v1/37/1.0"},
            {"SuperVinlin","https://static-cdn.jtvnw.net/emoticons/v1/31/1.0"},
            {"SwiftRage","https://static-cdn.jtvnw.net/emoticons/v1/34/1.0"},
            {"tbBaconBiscuit","https://static-cdn.jtvnw.net/emoticons/v1/44499/1.0"},
            {"tbChickenBiscuit","https://static-cdn.jtvnw.net/emoticons/v1/56879/1.0"},
            {"tbQuesarito","https://static-cdn.jtvnw.net/emoticons/v1/56883/1.0"},
            {"tbSausageBiscuit","https://static-cdn.jtvnw.net/emoticons/v1/56881/1.0"},
            {"tbSpicy","https://static-cdn.jtvnw.net/emoticons/v1/56882/1.0"},
            {"tbSriracha","https://static-cdn.jtvnw.net/emoticons/v1/56880/1.0"},
            {"TF2John","https://static-cdn.jtvnw.net/emoticons/v1/1899/1.0"},
            {"TheKing","https://static-cdn.jtvnw.net/emoticons/v1/50901/1.0"},
            {"TheRinger","https://static-cdn.jtvnw.net/emoticons/v1/18/1.0"},
            {"TheTarFu","https://static-cdn.jtvnw.net/emoticons/v1/70/1.0"},
            {"TheThing","https://static-cdn.jtvnw.net/emoticons/v1/7427/1.0"},
            {"ThunBeast","https://static-cdn.jtvnw.net/emoticons/v1/1898/1.0"},
            {"TinyFace","https://static-cdn.jtvnw.net/emoticons/v1/67/1.0"},
            {"TooSpicy","https://static-cdn.jtvnw.net/emoticons/v1/359/1.0"},
            {"TriHard","https://static-cdn.jtvnw.net/emoticons/v1/171/1.0"},
            {"TTours","https://static-cdn.jtvnw.net/emoticons/v1/38436/1.0"},
            {"twitchRaid","https://static-cdn.jtvnw.net/emoticons/v1/62836/1.0"},
            {"UleetBackup","https://static-cdn.jtvnw.net/emoticons/v1/49/1.0"},
            {"UncleNox","https://static-cdn.jtvnw.net/emoticons/v1/3666/1.0"},
            {"UnSane","https://static-cdn.jtvnw.net/emoticons/v1/71/1.0"},
            {"VaultBoy","https://static-cdn.jtvnw.net/emoticons/v1/54090/1.0"},
            {"Volcania","https://static-cdn.jtvnw.net/emoticons/v1/166/1.0"},
            {"WholeWheat","https://static-cdn.jtvnw.net/emoticons/v1/1896/1.0"},
            {"WinWaker","https://static-cdn.jtvnw.net/emoticons/v1/167/1.0"},
            {"WTRuck","https://static-cdn.jtvnw.net/emoticons/v1/1897/1.0"},
            {"WutFace","https://static-cdn.jtvnw.net/emoticons/v1/28087/1.0"},
            {"YouWHY","https://static-cdn.jtvnw.net/emoticons/v1/4337/1.0"}

        };
        for (int i = 0; i < kappas.length; i++) {
            if(msg.indexOf(kappas[i][0]) > -1) {
                msg = msg.replaceAll(kappas[i][0], "<img src=\"" + kappas[i][1] + "\" />");
            }
        }
        return msg;
    }

}
