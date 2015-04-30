package com.usp.icmc.tictactoe;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.xml.soap.Node;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;


public class HostConnectController{
    @FXML
    private TextField hostPortText;
    @FXML
    private TextField connectIPText;
    @FXML
    private TextField connectPortText;
    @FXML
    private RadioButton hostRadio;
    @FXML
    private RadioButton connectRadio;
    @FXML
    private Button hostButton;
    @FXML
    private Button connectButton;

    private final double gameWidth = 800;
    private final double gameHeight = 600;

    @FXML
    private void setRadioHost(Event e) {
        connectIPText.setEditable(false);
        connectPortText.setEditable(false);
        connectButton.setDisable(true);
        hostPortText.setEditable(true);
        hostButton.setDisable(false);
        //connectRadio.setSelected(false);
    }

    @FXML
    private void setRadioConnect(Event e) {
        connectIPText.setEditable(true);
        connectPortText.setEditable(true);
        connectButton.setDisable(false);
        hostPortText.setEditable(false);
        hostButton.setDisable(true);
        //hostRadio.setSelected(false);
    }

    @FXML
    private void waitForConnection(Event e) {

        int port;
        final ServerSocket socket;

        try {
            port = Integer.parseInt(hostPortText.getText());
        } catch (NumberFormatException numberFormatException) {
            /* TODO */
            return;
        }

        try {
            socket = new ServerSocket(port);
        } catch (IOException ioException) {
            /* TODO */
            return;
        }

        new Thread(() -> {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                socket.close();
                            } catch (IOException ioException) {
                                System.err.println("Not possible to close the socket");
                            }
                        }
                    },60000
            );
        }).start();

        try {
            ConnectionController.setConnection(socket.accept());

        } catch (SocketException socketException) {
            return;
        } catch (IOException ioException) {
            /* TODO */
            return;
        }

        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
        changeToGameScene(stage);
    }


    @FXML
    private void connectToServer(Event e){
        String IP;
        int port;

        try {
            port = Integer.parseInt(connectPortText.getText());
        } catch (NumberFormatException numberFormatException) {
            /* TODO */
            return;
        }

        IP = connectIPText.getText();

        try {
            ConnectionController.setConnection(new Socket(IP, port));
        } catch (UnknownHostException e1) {
            /* TODO */
            return;
        } catch (IOException e1) {
            /* TODO */
            return;
        }

        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
        changeToGameScene(stage);
    }

    private void changeToGameScene(Stage stage){
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("gameScene.fxml"));
        } catch (IOException e1) {
            /* TODO */
            return;
        }

        stage.setScene(new Scene(root, gameWidth, gameHeight));
        stage.setTitle("Tic Tac Toe");
        stage.show();
    }

}


