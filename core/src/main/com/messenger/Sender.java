package com.messenger;

import com.protocols.PMessage;
import com.protocols.PMessageType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

public class Sender extends Thread {

    private String destinationIP;
    private int destinationPort;
    private Socket client;
    private Pane root;
    @FXML private ScrollPane messageListView;
    private String messageToSend, sender;
    private ArrayList<String> messageList;
    Logger logger = LoggerFactory.getLogger(Sender.class);

    public Sender() {
    }

    public Sender(String destinationIP, int destinationPort) {
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.messageList = new ArrayList<String>();
    }

    @Override
    public void run() {
        try {
            logger.info("Sender waiting for " + destinationIP + " on port " + destinationPort);
            client = null;
            while (client == null) {
                try {
                    client = new Socket(destinationIP, destinationPort);
                } catch (IOException e) {
                    Thread.sleep(1000);
                }
            }
            logger.info("Sender just connected to " + client.getRemoteSocketAddress());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public int getSenderPort() {
        if (client != null)
            return client.getPort();
        return -1;
    }

    public void sendMessage(String message) throws IOException {
        logger.info("Enter sendMessage()");
    	this.messageToSend = message;

        messageList.add(messageToSend);


        PMessage createPMessage = new PMessage();
        createPMessage.setSender(sender);
        createPMessage.setType(PMessageType.USER);
        createPMessage.setMessage(message);
//        JSONObject jsonObject = createPMessage.toJSON();

        OutputStream os = client.getOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);

        out.writeObject(createPMessage);
        out.flush();

        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                Label messageLabel = new Label(messageToSend);
                Label senderLabel = new Label("You");
                senderLabel.setFont(new Font(10));
                senderLabel.setStyle("-fx-padding:2;-fx-background-color:#2c3e50;");
                senderLabel.setTextFill(Color.WHITE);
                senderLabel.setVisible(false);

                messageLabel.setOnMouseEntered(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(true);
                        messageLabel.setOpacity(0.9);
                    }
                });

                messageLabel.setOnMouseExited(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(false);
                        messageLabel.setOpacity(1.0);
                    }
                });
                messageLabel.setCursor(Cursor.HAND);
                messageLabel.setFont(new Font(15));
                messageLabel.setStyle("-fx-background-color:#ecf0f1;-fx-padding:10;-fx-background-radius:8;");
                messageLabel.setTextFill(Color.BLACK);
                VBox messageInfo = new VBox(messageLabel, senderLabel);
                BorderPane borderPane = new BorderPane();
                borderPane.setRight(messageInfo);
                Receiver.vbox.getChildren().add(borderPane);
                messageListView.setContent(Receiver.vbox);
            }
        });
		logger.info("Exit sendMessage()");
    }

    public void sendFile(byte[] byteArray, File file) throws IOException {
        //this.messageToSend = message;
        logger.info("Enter sendFile()");
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        bufferedInputStream.read(byteArray, 0, byteArray.length);
        String fileName = file.getName();
        OutputStream os = client.getOutputStream();

        PMessage pMessage = new PMessage();
        pMessage.setSender(sender);
        pMessage.setMessage(fileName);
        pMessage.setType(PMessageType.FILE);
        pMessage.setFile(Base64.getEncoder().encode(byteArray));

        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(pMessage);
        out.flush();

        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                Label messageLabel = new Label(fileName + " is sent");
                messageLabel.setFont(new Font(15));
                messageLabel.setStyle("-fx-background-color:#2ecc71;-fx-padding:10;-fx-background-radius:8;");
                messageLabel.setTextFill(Color.WHITE);
                Label senderLabel = new Label("You");
                senderLabel.setFont(new Font(10));
                senderLabel.setStyle("-fx-padding:2;-fx-background-color:#2c3e50;");
                senderLabel.setTextFill(Color.WHITE);
                senderLabel.setVisible(false);

                messageLabel.setOnMouseEntered(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(true);
                        messageLabel.setOpacity(0.9);
                    }
                });

                messageLabel.setOnMouseExited(new EventHandler<Event>() {

                    @Override
                    public void handle(Event event) {
                        senderLabel.setVisible(false);
                        messageLabel.setOpacity(1.0);
                    }
                });
                VBox messageInfo = new VBox(messageLabel, senderLabel);
                BorderPane borderPane = new BorderPane();
                borderPane.setRight(messageInfo);
                Receiver.vbox.getChildren().add(borderPane);
                messageListView.setContent(Receiver.vbox);

            }
        });
    }

    public void sendBackgroundColor(String color) throws IOException {
        logger.info("Enter sendBackgroundColor()");

        PMessage pMessage = new PMessage();
        pMessage.setSender(sender);
        pMessage.setType(PMessageType.COLOR);
        pMessage.setMessage(sender + "has changed color to " + color);
        pMessage.setColor(color);

        OutputStream os = client.getOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(pMessage);
        out.flush();

        ObservableList<Node> observableList = Receiver.vbox.getChildren();
        for (Node i : observableList) {
            BorderPane gotBorderPane = (BorderPane) i;
            VBox gotVBox = (VBox) gotBorderPane.getChildren().get(0);
            Label senderLabel = (Label) gotVBox.getChildren().get(1);
            if (!senderLabel.getText().equals("You")) {
                Label gotmessageLabel = (Label) gotVBox.getChildren().get(0);

                gotmessageLabel.setStyle("-fx-background-color:" + color + ";-fx-padding:10;-fx-background-radius:8;");
            }

        }

        logger.info("Exit sendBackgroundColor()");
    }


    public void setRoot(Pane root) {
        this.root = root;
        messageListView = (ScrollPane) root.lookup("#messageList");
        messageListView.setFitToWidth(true);
        messageListView.setStyle("-fx-control-inner-background: black;");
    }

    public ArrayList<String> getMessageList() {
        return messageList;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
