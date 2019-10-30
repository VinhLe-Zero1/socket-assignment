package com.messenger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

import com.protocols.PMessage;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Receiver extends Thread {


    private ServerSocket receiverSocket;
    private ArrayList<String> messageList;
    private String finalMessage, sender;
    private boolean connected;
    private Socket receiver;
    private Pane root;
    static VBox vbox;
    private Stage stage;
    @FXML
    private ScrollPane messageListView;
    Logger logger = LoggerFactory.getLogger(Receiver.class);

    public Receiver(int port) throws IOException {
        receiverSocket = new ServerSocket(port);
        receiverSocket.setSoTimeout(1000000);
        messageList = new ArrayList<String>();
        finalMessage = "";
        connected = false;
        vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setMaxWidth(600);
        vbox.setMinWidth(570);
    }


    @Override
    public void run() {

        logger.info("Receiver waiting for " + receiverSocket.getLocalSocketAddress() + " on port " + receiverSocket.getLocalPort());
        receiver = null;
        try {
            ObjectInputStream in;

            while ((receiver = receiverSocket.accept()) != null) {

                while (true) {
                    InputStream inputStream = receiver.getInputStream();
                    in = new ObjectInputStream(inputStream);

                    PMessage receivedPMessage = (PMessage) in.readObject();

                    logger.info(receivedPMessage.getType() + " - " + receivedPMessage.getMessage());
                    switch (receivedPMessage.getType()) {
                        case USER:
                            String message = receivedPMessage.getMessage();
                            sender = receivedPMessage.getSender();
                            for (String msg : messageList) {
                                logger.info(msg);
                            }
                            finalMessage = message;
                            messageList.add(finalMessage);

                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {

                                    Label messageLabel = new Label(finalMessage);
                                    //								MessengerController.allMessages.add(sender + " : " + finalMessage);
                                    Label senderLabel = new Label(sender);
                                    senderLabel.setFont(new Font(10));
                                    senderLabel.setStyle("-fx-padding:2;-fx-background-color:#2c3e50;");
                                    senderLabel.setTextFill(Color.WHITE);
                                    senderLabel.setVisible(false);
                                    messageLabel.setFont(new Font(15));
                                    messageLabel.setStyle("-fx-background-color:#e67e22;-fx-padding:10;-fx-background-radius:8;");
                                    messageLabel.setTextFill(Color.WHITE);
                                    messageLabel.setCursor(Cursor.HAND);
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
                                    borderPane.setLeft(messageInfo);
                                    vbox.getChildren().add(borderPane);
                                    messageListView.setContent(vbox);
                                }
                            });
                        	break;
						case FILE:
							String fileStream = new String(receivedPMessage.getFile(), "UTF-8");
							String fileName = receivedPMessage.getMessage();
							sender = receivedPMessage.getSender();
							byte[] byteArray = Base64.getDecoder().decode(fileStream);

							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									Label messageLabel = new Label(fileName + " Received");
									messageLabel.setFont(new Font(15));
									messageLabel.setStyle("-fx-background-color:#e67e22;-fx-padding:10;-fx-background-radius:8;");
									messageLabel.setTextFill(Color.WHITE);
									messageLabel.setUnderline(true);
									Label senderLabel = new Label(sender);
									senderLabel.setFont(new Font(10));
									senderLabel.setStyle("-fx-padding:2;-fx-background-color:#2c3e50;");
									senderLabel.setTextFill(Color.WHITE);
									senderLabel.setVisible(false);
									VBox messageInfo = new VBox(messageLabel, senderLabel);

									BorderPane borderPane = new BorderPane();
									borderPane.setLeft(messageInfo);
									vbox.getChildren().add(borderPane);
									messageListView.setContent(vbox);
									messageLabel.setCursor(Cursor.HAND);

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
									messageLabel.setOnMouseClicked(new EventHandler<Event>() {

										@Override
										public void handle(Event event) {
											FileChooser fileSaver = new FileChooser();
											fileSaver.getExtensionFilters().addAll(
													new FileChooser.ExtensionFilter("Text File",
															"*.txt"),
													new FileChooser.ExtensionFilter("Document",
															"*.pdf", "*.docx"),
													new FileChooser.ExtensionFilter("Image Files",
															"*.jpg", "*.png", "*.bmp", "*.gif"),
													new FileChooser.ExtensionFilter("Video Files",
															"*.mkv", "*.mp4"),
													new FileChooser.ExtensionFilter("Audio Files",
															"*.mp3", "*.m4p")
											);
											File file = fileSaver.showSaveDialog(stage);
											if (file != null) {
												logger.info(file.getAbsolutePath());
												try {
													saveFile(file.getAbsolutePath(), byteArray);
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}
									});
								}
							});
							break;
						case COLOR:
							String hexColor = receivedPMessage.getColor();
							ObservableList<Node> observableList = vbox.getChildren();
							for(Node i : observableList)
							{
								BorderPane gotBorderPane = (BorderPane) i;
								VBox gotVBox = (VBox) gotBorderPane.getChildren().get(0);
								Label sederLabel = (Label) gotVBox.getChildren().get(1);
								if(!sederLabel.getText().equals("You"))
									gotVBox.getChildren().get(0).setStyle("-fx-background-color: "+hexColor+";-fx-padding:10;-fx-background-radius:8;");
							}
							break;
						default:
							logger.info("Unable to handle type of message!");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int getReceiverPort() {
        if (receiverSocket != null)
            return receiverSocket.getLocalPort();
        return -1;
    }

    public void setRoot(Pane root) {
        this.root = root;
        messageListView = (ScrollPane) root.lookup("#messageList");
        messageListView.setHbarPolicy(ScrollBarPolicy.NEVER);
        messageListView.setVbarPolicy(ScrollBarPolicy.NEVER);
        messageListView.setFitToWidth(true);
        messageListView.setStyle("-fx-control-inner-background: black;");

    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void saveFile(String filePath, byte[] byteArray) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        bufferedOutputStream.write(byteArray, 0, byteArray.length);
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        Label infoMessage = new Label("File is successfully saved as " + filePath);
        ;
        infoMessage.setStyle("-fx-font-size:15;-fx-padding:10;");
        Label openLink = new Label("Click Here To Open");
        openLink.setCursor(Cursor.HAND);
        openLink.setStyle("-fx-font-size:15;-fx-padding:10;-fx-text-fill:#3498db;");
        openLink.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + filePath);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        VBox vbox = new VBox(infoMessage, openLink);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Congratulation");
        alert.getDialogPane().setContent(vbox);
        alert.show();
    }


    public ArrayList<String> getMessageList() {
        return messageList;
    }


}
