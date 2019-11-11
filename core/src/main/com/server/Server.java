package com.server;

import com.protocols.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server implements Runnable{

    /* Setting up variables */
    private static int PORT;
    private static final HashMap<String, User> names = new HashMap<>();
    private static HashMap<String, ObjectOutputStream> writers = new HashMap<>();
    private static ArrayList<User> users = new ArrayList<>();
    private static HashMap<String, InetAddress> addresses = new HashMap<>();
    static Logger logger = LoggerFactory.getLogger(Server.class);

    private static ServerSocket listener;


    public static ServerSocket getListener() {
        return listener;
    }


    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        String port = null;
        do {
            if (port !=  null) System.out.println("Please enter valid port!");
            System.out.print("Enter PORT: ");
            port = scanner.nextLine();
        }
        while (Integer.parseInt(port) < 1024 || Integer.parseInt(port) > 65365);
        PORT = Integer.parseInt(port);
        logger.info("The chat server is running on " + PORT);
        listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    public Server(String port) {
        PORT = Integer.parseInt(port);
    }

    @Override
    public void run() {
        logger.info("The chat server is running on " + PORT);

        try {
            listener = new ServerSocket(PORT);
            while (!listener.isClosed()) {
                new Handler(listener.accept()).start();
            }
            listener.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info("The chat server is closed on " + PORT);
        }

    }


    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private Logger logger = LoggerFactory.getLogger(Handler.class);
        private User user;
        private String channel;
        private ObjectInputStream input;
        private OutputStream os;
        private ObjectOutputStream output;
        private InputStream is;

        public Handler(Socket socket) throws IOException {
            this.socket = socket;
            this.channel = "#Community";
        }

        public void run() {
            logger.info("Attempting to connect a user...");
            try {
                is = socket.getInputStream();
                input = new ObjectInputStream(is);
                os = socket.getOutputStream();
                output = new ObjectOutputStream(os);

                SMessage firstSMessage = (SMessage) input.readObject();
                checkDuplicateUsername(firstSMessage);
                writers.put(name, output);
                sendNotification(firstSMessage);
                addToList();

                while (socket.isConnected()) {
                    SMessage inputmsg = (SMessage) input.readObject();
                    if (inputmsg != null) {
//                        logger.info(inputmsg.getType() + " - " + name + " -> " + channel);
                        switch (inputmsg.getType()) {
                            case USER:
                                write(inputmsg);
                                break;
                            case FILE:
                                write(inputmsg);
                                break;
                            case VOICE:
                                logger.info(inputmsg.getType() + " - " + name + " -> " + channel + ": " + inputmsg.getVoiceMsg().length);
                                write(inputmsg);
                                break;
                            case CONNECTED:
                                addToList();
                                break;
                            case STATUS:
                                changeStatus(inputmsg);
                                break;
                            case CHANNEL:
                                changeChannel(inputmsg);
                                break;
                            case PICTURE:
                                logger.info(inputmsg.getType() + " - " + name + " -> " + channel + ": " + inputmsg.getPictureMsg().length);
                                write(inputmsg);
                                break;
                            case OPENP2P:
                                openP2P(inputmsg);
                                break;
                            case CLOSEP2P:
                                closeP2P(inputmsg);
                                break;
                            case DISCONNECTED:
                                socket.close();
                                break;
                            default:
                                logger.warn("Message ignored cause uncaught  UnknownType!");
                        }

                    }
                }
            } catch (SocketException socketException) {
                logger.error("Socket Exception for user " + name);
            } catch (DuplicateUsernameException duplicateException){
                logger.error("Duplicate Username : " + name);
            } catch (Exception e){
                logger.error("Exception in run() method for user: " + name, e);
            } finally {
                closeConnections();
            }
        }

        private SMessage closeP2P(SMessage inputmsg) throws IOException {
            logger.info("Close P2P " + inputmsg.getName() + "-" + inputmsg.getPeer().getName());
            SMessage msg = new SMessage();
            msg.setName(user.getName());
            msg.setPeer(inputmsg.getPeer());
            msg.setType(SMessageType.CLOSEP2P);
            msg.setMessage("");
            writers.get(inputmsg.getPeer().getName()).writeObject(msg);
            writers.get(inputmsg.getPeer().getName()).reset();
            return msg;
        }

        private SMessage openP2P(SMessage inputmsg) throws IOException {
            logger.info("Open P2P " + inputmsg.getName() + "-" + inputmsg.getPeer().getName());
            Peer peer = inputmsg.getPeer();
//            peer.setSourceHost(addresses.get(peer.getName()).getHostAddress());
            SMessage msg = new SMessage();
            msg.setName(user.getName());

            msg.setPeer(inputmsg.getPeer());
            msg.setType(SMessageType.OPENP2P);
            msg.setMessage("");
            writers.get(inputmsg.getPeer().getName()).writeObject(msg);
            writers.get(inputmsg.getPeer().getName()).reset();
            return msg;
        }

        private SMessage changeStatus(SMessage inputmsg) throws IOException {
            logger.info(inputmsg.getName() + " has changed status to  " + inputmsg.getStatus());
            SMessage msg = new SMessage();
            msg.setName(user.getName());
            msg.setType(SMessageType.STATUS);
            msg.setMessage("");
            User userObj = names.get(name);
            userObj.setStatus(inputmsg.getStatus());
            write(msg);
            return msg;
        }

        private void changeChannel(SMessage inputmsg) throws  IOException {
            logger.info(inputmsg.getName() + " has changed channel to  " + inputmsg.getChannel());
            this.channel = inputmsg.getChannel();
        }

        private synchronized void checkDuplicateUsername(SMessage firstSMessage) throws DuplicateUsernameException {
            logger.info(firstSMessage.getName() + " is trying to connect");
            if (!names.containsKey(firstSMessage.getName())) {
                this.name = firstSMessage.getName();
                user = new User();
                user.setName(firstSMessage.getName());
                user.setStatus(Status.ONLINE);
                user.setPicture(firstSMessage.getPicture());
                users.add(user);
                names.put(name, user);
                addresses.put(name, socket.getInetAddress());

                logger.info(name +  " has been added to the list");
            } else {
                logger.error(firstSMessage.getName()  + " is already connected");
                throw new DuplicateUsernameException(firstSMessage.getName() + " is already connected");
            }
        }

        private SMessage sendNotification(SMessage firstSMessage) throws IOException {
            SMessage msg = new SMessage();
            msg.setMessage("has joined the chat.");
            msg.setType(SMessageType.NOTIFICATION);
            msg.setName(firstSMessage.getName());
            msg.setPicture(firstSMessage.getPicture());
            write(msg);
            return msg;
        }


        private SMessage removeFromList() throws IOException {
            logger.debug("removeFromList() method Enter");
            SMessage msg = new SMessage();
            msg.setMessage("has left the chat.");
            msg.setType(SMessageType.DISCONNECTED);
            msg.setName("SERVER");
            msg.setUserlist(names);
            write(msg);
            logger.debug("removeFromList() method Exit");
            return msg;
        }

        /*
         * For displaying that a user has joined the server
         */
        private SMessage addToList() throws IOException {
            SMessage msg = new SMessage();
            msg.setMessage("Welcome, You have now joined the server! Enjoy chatting!");
            msg.setType(SMessageType.CONNECTED);
            msg.setName("SERVER");
            write(msg);
            return msg;
        }

        /*
         * Creates and sends a Message type to the listeners.
         */
        private void write(SMessage msg) throws IOException {
            for (Map.Entry writer : writers.entrySet()) {
                if (channel.equals(writer.getKey().toString()) || channel.equals("#Community") || name.equals(writer.getKey().toString())) {
                    msg.setUserlist(names);
                    msg.setUsers(users);
                    msg.setOnlineCount(names.size());
                    msg.setChannel(channel);
                    writers.get(writer.getKey()).writeObject(msg);
                    writers.get(writer.getKey()).reset();
                }
            }
        }

        /*
         * Once a user has been disconnected, we close the open connections and remove the writers
         */
        private synchronized void closeConnections()  {
            logger.debug("closeConnections() method Enter");
            logger.info("HashMap names:" + names.size() + " writers:" + writers.size() + " usersList size:" + users.size());
            if (name != null) {
                names.remove(name);
                logger.info("User: " + name + " has been removed!");
            }
            if (user != null){
                users.remove(user);
                logger.info("User object: " + user + " has been removed!");
            }
            if (output != null){
                writers.remove(name);
                logger.info("Writer object: " + user + " has been removed!");
            }
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                removeFromList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("HashMap names:" + names.size() + " writers:" + writers.size() + " usersList size:" + users.size());
            logger.debug("closeConnections() method Exit");
        }
    }
}
