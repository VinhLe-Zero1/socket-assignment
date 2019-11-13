package com.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.lobby.community.ListenerIO;
import com.protocols.PMessage;
import com.protocols.SMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;


public class ServerIO {
    private static HashMap<UUID, ListenerIO> ioListeners = new HashMap<>();

    public static SocketIOServer server;

    Logger logger = LoggerFactory.getLogger(ServerIO.class);

    private static String getRealIP() throws SocketException {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws SocketException {
        String host = getRealIP();
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(7070);
        config.setMaxFramePayloadLength(26214400);
        server = new SocketIOServer(config);

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                System.out.println("New user wait for connecting..");

                System.out.println(client.getSessionId());
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                System.out.println("onDisconnected");
                try {
                    ioListeners.get(client.getSessionId()).disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ioListeners.remove(client.getSessionId());
            }
        });

        server.addEventListener("join", SMessage.class, new DataListener<SMessage>() {

            @Override
            public void onData(SocketIOClient client, SMessage data, AckRequest ackSender) throws Exception {
                try {
                    if (checkDupplicate(client.getSessionId())) {
                        client.sendEvent("error", "Can't have more than 1 app on machine");
                        return;
                    }
                } catch (DuplicateUsernameException e) {
                    e.printStackTrace();
                }

                String imageSrc = "images/default.png";

                if (data.getName().substring(0,1).matches("[a-zA-Z]"))
                    imageSrc = "images/alphabet/" + data.getName().substring(0,1).toLowerCase() + ".png";

                ListenerIO listener = new ListenerIO(host, 9001, data.getName(), client.getSessionId(), imageSrc);

                ioListeners.put(client.getSessionId(), listener);
                Thread x = new Thread(listener);
                x.start();
                System.out.println("Joined: " + data.getName());
            }
        });

        server.addEventListener("sendMessage", SMessage.class, new DataListener<SMessage>() {
            @Override
            public void onData(SocketIOClient client, SMessage sMessage, AckRequest ackRequest) throws Exception {
                if (ioListeners.containsKey(client.getSessionId())) {
                    System.out.println(client.getSessionId() + " sent a message!");
                    ioListeners.get(client.getSessionId()).send(sMessage.getMessage());

                }
                else
                    client.sendEvent("error", "Listener closed!");
            }
        });

        server.addEventListener("changeChannel", SMessage.class, new DataListener<SMessage>() {
            @Override
            public void onData(SocketIOClient client, SMessage sMessage, AckRequest ackRequest) throws Exception {
                if (ioListeners.containsKey(client.getSessionId()))
                    ioListeners.get(client.getSessionId()).send(sMessage.getMessage());
                else
                    client.sendEvent("error", "Listener closed!");
            }
        });

        server.addEventListener("sendStatus", SMessage.class, new DataListener<SMessage>() {
            @Override
            public void onData(SocketIOClient client, SMessage sMessage, AckRequest ackRequest) throws Exception {
                if (ioListeners.containsKey(client.getSessionId()))
                    ioListeners.get(client.getSessionId()).sendStatusUpdate(sMessage.getStatus());
                else
                    client.sendEvent("error", "Listener closed!");
            }
        });

        server.addEventListener("sendImage", SMessage.class, new DataListener<SMessage>() {
            @Override
            public void onData(SocketIOClient client, SMessage sMessage, AckRequest ackRequest) throws Exception {
                if (ioListeners.containsKey(client.getSessionId())) {
                    byte[] byteBase64 = sMessage.getMessage().getBytes();

                    ioListeners.get(client.getSessionId()).sendPicture(byteBase64);
                }
                else
                    client.sendEvent("error", "Listener closed!");
            }
        });

        server.addEventListener("sendFile", SMessage.class, new DataListener<SMessage>() {
            @Override
            public void onData(SocketIOClient client, SMessage sMessage, AckRequest ackRequest) throws Exception {
                if (ioListeners.containsKey(client.getSessionId())) {

                    byte[] byteBase64 = sMessage.getMessage().getBytes();
                    ioListeners.get(client.getSessionId()).sendFile(sMessage.getName(), byteBase64);
                }
                else
                    client.sendEvent("error", "Listener closed!");
            }
        });


        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started");

    }

    private static boolean checkDupplicate(UUID sessionId) throws DuplicateUsernameException {
//        if (ioListeners.containsKey(sessionId))
//            throw new DuplicateUsernameException("Only run an unique app on machine.");
        return ioListeners.containsKey(sessionId);
    }
}
