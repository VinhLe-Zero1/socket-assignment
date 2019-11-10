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

import java.util.HashMap;
import java.util.UUID;


public class ServerIO {
    private static HashMap<String, ListenerIO> ioListeners = new HashMap<>();
    private static HashMap<String, UUID> sessionIds = new HashMap<>();

    public static ServerIO instance;

    Logger logger = LoggerFactory.getLogger(ServerIO.class);

    public ServerIO() {
        instance = this;
    }


    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(7070);
        final SocketIOServer server = new SocketIOServer(config);

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
            }
        });

        server.addEventListener("send", PMessage.class, new DataListener<PMessage>() {

            @Override
            public void onData(SocketIOClient client, PMessage data, AckRequest ackSender) throws Exception {
                System.out.println("onSend: " + data.toString());
                server.getBroadcastOperations().sendEvent("message", data);
            }
        });


        server.addEventListener("join", SMessage.class, new DataListener<SMessage>() {

            @Override
            public void onData(SocketIOClient client, SMessage data, AckRequest ackSender) throws Exception {
                checkDupplicate(client, data);

                ListenerIO listener = new ListenerIO("localhost", 9001, data.getName(), null, instance);

                sessionIds.put(data.getName(), client.getSessionId());
                ioListeners.put(data.getName(), listener);
                Thread x = new Thread(listener);
                x.start();

                System.out.println("Joined: " + data.getName());
                server.getBroadcastOperations().sendEvent("message", data);
            }
        });
        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started");

    }

    private static void checkDupplicate(SocketIOClient client, SMessage data) throws DuplicateUsernameException {
        if (sessionIds.get(data.getName()) != null)
            throw new DuplicateUsernameException("Username already token");
    }

    public void send(SMessage sMessage) {

    }
}
