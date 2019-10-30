package com.messenger;
//import com.messages.PMessage;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import com.protocols.PMessage;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.shape.SVGPath;
import javafx.stage.WindowEvent;

import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Time;
import java.util.ArrayList;

import java.util.Random;

public class MessengerController {


    @FXML
    private ImageView messageSendButton;
    @FXML
    private TextArea messageSendBox;
    @FXML
    private ImageView fileAttachButton;
    @FXML
    private ColorPicker themeChangeButton;
    @FXML
    private ScrollPane messageList;
    @FXML
    private Pane background;

    private Sender sender;
    private Receiver receiver;
    private Pane root;
    private Stage stage;
    public static ArrayList<String> allMessages;
    private FileChooser fileChooser;
    private File file;
    private byte[] byteArray;

    Logger logger = LoggerFactory.getLogger(MessengerController.class);

    public MessengerController() {
        this.sender = null;
        this.receiver = null;
    }

    public void linkSenderAndReceiver(Sender sender, Receiver receiver) {
        this.sender = sender;
        this.receiver = receiver;

        if (!this.sender.isAlive()) this.sender.start();
        if (!this.receiver.isAlive()) this.receiver.start();
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    public String SVGPathGen(int ix, int iy,int isize){
        String path = "";
        String x = String.valueOf(ix);
        String y = String.valueOf(iy);
        String size = String.valueOf(isize);
        path += "M "+ x + " " + y +" "  + " a 1 1 1 0 1 " + size + " 0 a 1 1 0 0 1 "+size+" 0 l -"+size+" "+size+" z";
        return path;
    }

    public void generateAnimation(){
        Random rand = new Random();

        int sizeOfHeart = rand.nextInt(25) + 1;
        int speedOfHeart = rand.nextInt(10) + 1;
        int startXPoint = rand.nextInt(700);
        int startYPoint = rand.nextInt(700);
        int direction = rand.nextInt(5) + 1;
        //rectParallel.setTranslateX(50);
        //rectParallel.setTranslateY(75);
        SVGPath svgImg = new SVGPath();
        TranslateTransition translateTransition = null;
        switch (direction){
            case 1:
                // Move left 2 right
                svgImg.setContent(SVGPathGen(0,startYPoint,sizeOfHeart));
                svgImg.setTranslateX(0);
                translateTransition = new TranslateTransition(Duration.millis(speedOfHeart*1000), svgImg);
                translateTransition.setFromX(0);
                translateTransition.setToX(700 - sizeOfHeart);
                translateTransition.setCycleCount(Timeline.INDEFINITE);
                translateTransition.setAutoReverse(true);
                break;

            case 2:
                // Move top 2 bottom
                svgImg.setContent(SVGPathGen(startXPoint,0,sizeOfHeart));
                svgImg.setTranslateY(0);
                translateTransition = new TranslateTransition(Duration.millis(speedOfHeart*1000), svgImg);
                translateTransition.setFromY(0);
                translateTransition.setToY(700 - sizeOfHeart);
                translateTransition.setCycleCount(Timeline.INDEFINITE);
                translateTransition.setAutoReverse(true);
                break;

            case 3:
                // Move left 2 right, top 2 bottom
                svgImg.setContent(SVGPathGen(startXPoint,0,sizeOfHeart));
                svgImg.setTranslateX(0);
                svgImg.setTranslateY(0);
                translateTransition = new TranslateTransition(Duration.millis(speedOfHeart*1000), svgImg);
                translateTransition.setFromX(0);
                translateTransition.setToX(700 - sizeOfHeart);
                translateTransition.setFromY(0);
                translateTransition.setToY(700-sizeOfHeart);
                translateTransition.setCycleCount(Timeline.INDEFINITE);
                translateTransition.setAutoReverse(true);
                break;

            case 4:
                // Move bottom 2 top
                svgImg.setContent(SVGPathGen(0,startYPoint,sizeOfHeart));
                svgImg.setTranslateY(700 - sizeOfHeart);
                translateTransition = new TranslateTransition(Duration.millis(speedOfHeart*1000), svgImg);
                translateTransition.setFromY(700 - sizeOfHeart);
                translateTransition.setToY(0);
                translateTransition.setCycleCount(Timeline.INDEFINITE);
                translateTransition.setAutoReverse(true);
                break;

            case 5:
                // Move right 2 left
                svgImg.setContent(SVGPathGen(0,startYPoint,sizeOfHeart));
                svgImg.setTranslateX(700 - sizeOfHeart);
                translateTransition = new TranslateTransition(Duration.millis(speedOfHeart*1000), svgImg);
                translateTransition.setFromX(700 - sizeOfHeart);
                translateTransition.setToX(0);
                translateTransition.setCycleCount(Timeline.INDEFINITE);
                translateTransition.setAutoReverse(true);
                break;

            case 6:
                // Move left 2 right
                svgImg.setContent(SVGPathGen(0,startYPoint,sizeOfHeart));
                svgImg.setTranslateX(700 - sizeOfHeart);
                svgImg.setTranslateY(700 - sizeOfHeart);
                translateTransition = new TranslateTransition(Duration.millis(speedOfHeart*1000), svgImg);
                translateTransition.setFromX(700 - sizeOfHeart);
                translateTransition.setToX(0);
                translateTransition.setFromY(700 - sizeOfHeart);
                translateTransition.setToY(0);
                translateTransition.setCycleCount(Timeline.INDEFINITE);
                translateTransition.setAutoReverse(true);
                break;
        }

        svgImg.setFill(Color.web("#bbd9dd"));
        svgImg.setOpacity(0.2);
        translateTransition.play();
        background.getChildren().add(background.getChildren().size()-1,svgImg);
    }

    public void initialize() {

        if (root != null && stage != null) {
            receiver.setRoot(root);
            sender.setRoot(root);
            sender.setSender("userName");
            receiver.setStage(stage);
            stage.setResizable(false);
        }



        themeChangeButton.setValue(Color.RED);
        messageList.setStyle("-fx-background:transparent;");

        messageSendButton.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event arg0) {
                String message = messageSendBox.getText().toString();
                try {
                    if (message.length() > 0) {
                        sender.sendMessage(message);
                    }
                    messageSendBox.setText("");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        /* Added to prevent the enter from adding a new line to inputMessageBox */
        messageSendBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                String message = messageSendBox.getText().toString();
                try {
                    if (message.length() > 0) {
                        sender.sendMessage(message);
                    }
                    messageSendBox.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });

        fileAttachButton.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                fileChooser = new FileChooser();
                file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    String fileName = file.getName();
                    byteArray = new byte[(int) file.length()];
                    try {
                        sender.sendFile(byteArray, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        themeChangeButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Color color = themeChangeButton.getValue();

                String hexColor = "#" + Integer.toHexString(color.hashCode());

                try {
                    sender.sendBackgroundColor(hexColor);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });


        int numberOfSquares = 40;
        while (numberOfSquares > 0){
            generateAnimation();
            numberOfSquares--;
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void saveFile(String filePath, byte[] byteArray, boolean isExiting) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        bufferedOutputStream.write(byteArray, 0, byteArray.length);
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        if (!isExiting) {
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
                        e.printStackTrace();
                    }

                }
            });
            VBox vbox = new VBox(infoMessage, openLink);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Congratulation");
            alert.getDialogPane().setContent(vbox);
            alert.show();
        }
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }


    public void showScene() {
        stage.show();
    }
    public void newSendMessage(PMessage createPMessage) {

    }
}
