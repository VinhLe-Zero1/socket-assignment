package com.lobby.community;

import com.lobby.login.LoginController;
import com.protocols.*;
import com.messenger.Receiver;
import com.messenger.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

import static com.protocols.SMessageType.CONNECTED;

public class Listener implements Runnable{

    private static final String HASCONNECTED = "has connected";
    private static final String COMMUNITY = "#Community";
    private static final String COMMUNITY_IMAGE = "images/alphabet/#.png";
    private static Random random;

    private static HashMap<String, Integer> peers = new HashMap<>();

    public static User community;
    private static String picture;
    private Socket socket;
    public static String hostname;
    public int port;
    public static String username;
    public CommunityController controller;
    private static ObjectOutputStream oos;
    public static String channel;
    private InputStream is;
    private ObjectInputStream input;
    private OutputStream outputStream;
    private static String ipAddress;
    Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, String username, String picture, CommunityController controller) throws UnknownHostException, SocketException {
        this.port = port;
        this.controller = controller;
        Listener.random = new Random();
        Listener.ipAddress = getRealIP();
        Listener.hostname = hostname;
        Listener.username = username;
        Listener.picture = picture;
        Listener.community = new User(COMMUNITY, COMMUNITY_IMAGE, "ONLINE");
        Listener.channel = "#Community";
    }



    private String getRealIP() throws SocketException {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }




    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            outputStream = socket.getOutputStream();
            oos = new ObjectOutputStream(outputStream);
            is = socket.getInputStream();
            input = new ObjectInputStream(is);
        } catch (IOException e) {
            LoginController.getInstance().showErrorDialog("Could not connect to server");
            logger.error("Could not Connect");
        }
        logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            connect();
            logger.info("Sockets in and out ready!");
            while (socket.isConnected()) {
                SMessage sMessage = null;
                sMessage = (SMessage) input.readObject();

                if (sMessage != null) {
                    logger.info("Message recieved:" + sMessage.getMessage() + " MessageType:" + sMessage.getType() + " Name:" + sMessage.getName() + " Channel: " + sMessage.getChannel());
                    switch (sMessage.getType()) {
                        case USER:
                            controller.addToChat(sMessage);
                            break;
                        case FILE:
                            controller.addToChat(sMessage);
                            break;
                        case VOICE:
                            controller.addToChat(sMessage);
                            break;
                        case NOTIFICATION:
                            controller.newUserNotification(sMessage);
                            break;
                        case SERVER:
                            controller.addAsServer(sMessage);
                            break;
                        case CONNECTED:
                            controller.setUserList(sMessage);
                            break;
                        case DISCONNECTED:
                            controller.setUserList(sMessage);
                            break;
                        case STATUS:
                            controller.setUserList(sMessage);
                            break;
                        case PICTURE:
                            controller.addToChat(sMessage);
                            break;
                        case OPENP2P:
                            if (!peers.containsKey(sMessage.getName()))
                                openP2PConnection(sMessage.getName());
                            waitForConnection(sMessage);
                            break;
                        case CLOSEP2P:
                            if (peers.containsKey(sMessage.getName()))
                                closeP2PConnection(sMessage.getName());
                            controller.closeMessenger(sMessage);
                        case CHANNEL:
                            break;
                    }
                }
            }
            logger.info("Disconnected");
            input.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            controller.logoutScene();
        }

    }

    public static void sendFileMessage(File file) throws IOException {
        byte[] byteArray = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(byteArray, 0, byteArray.length);
        String fileName = file.getName();

        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setMessage(fileName);
        createSMessage.setType(SMessageType.FILE);
        createSMessage.setStatus(Status.AWAY);
        createSMessage.setFileMsg(Base64.getEncoder().encode(byteArray));
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    private void waitForConnection(SMessage sMessage) throws IOException {
        controller.openMessenger(sMessage,
                new Sender(sMessage.getPeer().getSourceHost(), peers.get(sMessage.getName())),
                new Receiver(sMessage.getPeer().getSourcePort()));
    }

    public static void closeP2PConnection(String name) throws IOException {
        System.out.println(("Close P2P connection to " + name));
        if (peers.containsKey(name)) {
            Peer peer = new Peer();
            peer.setName(name);
            peer.setSourceHost(ipAddress);
            peer.setSourcePort(peers.get(name));

            SMessage createSMessage = new SMessage();
            createSMessage.setName(username);
            createSMessage.setType(SMessageType.CLOSEP2P);
            createSMessage.setPeer(peer);
            oos.writeObject(createSMessage);
            oos.flush();

            peers.remove(name);
        }
    }



    public static void openP2PConnection(String name) throws IOException {
        System.out.println(("Open P2P connection to " + name));
        if (!peers.containsKey(name)) {
            int randPort;
            do randPort = random.nextInt(11111) + 11111; while (peers.containsValue(randPort));
            Peer peer = new Peer();
            peer.setName(name);
            peer.setSourceHost(ipAddress);
            peer.setSourcePort(randPort);

            peers.put(name,randPort);

            SMessage createSMessage = new SMessage();
            createSMessage.setName(username);
            createSMessage.setType(SMessageType.OPENP2P);
            createSMessage.setPeer(peer);
            oos.writeObject(createSMessage);
            oos.flush();
        }
    }


    public static void sendPicture(byte[] base64Image) throws IOException{
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.PICTURE);
        createSMessage.setStatus(Status.AWAY);
        createSMessage.setPictureMsg(base64Image);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /*
    *   This method is used for sending message update channel
    *   @param msg -
     */
    public static void sendChannelUpadte(String updatedChannel) throws IOException {
        channel = updatedChannel;
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.CHANNEL);
        createSMessage.setChannel(updatedChannel);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /* This method is used for sending a normal Message
     * @param msg - The message which the user generates
     */
    public static void send(String msg) throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.USER);
        createSMessage.setStatus(Status.AWAY);
        createSMessage.setMessage(msg);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /* This method is used for sending a voice Message
 * @param msg - The message which the user generates
 */
    public static void sendVoiceMessage(byte[] audio) throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.VOICE);
        createSMessage.setStatus(Status.AWAY);
        createSMessage.setVoiceMsg(audio);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /* This method is used for sending a normal Message
 * @param msg - The message which the user generates
 */
    public static void sendStatusUpdate(Status status) throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(SMessageType.STATUS);
        createSMessage.setStatus(status);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
        oos.flush();
    }

    /* This method is used to send a connecting message */
    public static void connect() throws IOException {
        SMessage createSMessage = new SMessage();
        createSMessage.setName(username);
        createSMessage.setType(CONNECTED);
        createSMessage.setMessage(HASCONNECTED);
        createSMessage.setPicture(picture);
        oos.writeObject(createSMessage);
    }

}
