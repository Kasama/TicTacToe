package com.usp.icmc.tictactoe;

import com.sun.jnlp.ApiDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class HostConnectController {
    @FXML public ToggleGroup selectRadio;
    @FXML private TextField hostPortText;
    @FXML private TextField hostIPText;
    @FXML private TextField connectIPText;
    @FXML private TextField connectPortText;
    @FXML private RadioButton hostRadio;
    @FXML private RadioButton connectRadio;
    @FXML private Button hostButton;
    @FXML private Button connectButton;
    @FXML private ProgressIndicator hostIndicator;

    private final double gameWidth = 800;
    private final double gameHeight = 600;
    private Socket connection;
    private ThreadGroup threadGroup;

    @FXML
    private void setRadioHost(Event e) {
        connectIPText.setDisable(true);
        connectPortText.setDisable(true);
        connectButton.setDisable(true);
        hostPortText.setDisable(false);
        hostButton.setDisable(false);
    }

    @FXML
    private void setRadioConnect(Event e) {
        connectIPText.setDisable(false);
        connectPortText.setDisable(false);
        connectButton.setDisable(false);
        hostPortText.setDisable(true);
        hostButton.setDisable(true);
    }

    @FXML
    private void waitForConnection(Event e) {

        int port;
        final ServerSocket socket;
        threadGroup = new ThreadGroup("AcceptConnection");

        try {
            port = getPortFromTextField(hostPortText);
        } catch (NumberFormatException numberFormatException) {
            /* TODO dialog to show that the entered port is invalid*/
            System.out.println("Invalid port number");
            return;
        }

        try {
            socket = new ServerSocket(port);
        } catch (IOException ioException) {
            /* TODO shouldn't happen since the port is checked above*/
            return;
        }

        Timer timer = new Timer();
        Thread handleServerAcceptanceTimeout = new Thread(threadGroup, () -> {
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                socket.close();
                                hostIndicator.setOpacity(0d);
                            } catch (IOException ioException) {
                                System.err.println("Not possible to close the socket");
                            }
                        }
                    },10000
            );
        });
        handleServerAcceptanceTimeout.setDaemon(true);
        handleServerAcceptanceTimeout.start();

        hostIndicator.setOpacity(1d);
        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
        Thread handleServerAcceptance = new Thread(threadGroup, () -> {
            try {
                connection = socket.accept();
            } catch (SocketException socketException) {
                System.out.println("closed socket");
                return;
            } catch (IOException ioException) {
                /* TODO dialog to say that server got fucked up*/
                return;
            } finally {
                timer.cancel();
                handleServerAcceptanceTimeout.interrupt();
            }

            Platform.runLater(() -> {
                System.out.println(this.toString());
                changeToGameScene(stage, connection, true);
            });
            System.out.println("got a socket");
        });
        handleServerAcceptance.setDaemon(true);
        handleServerAcceptance.start();
//        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
//        new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                Socket connection;
//                try {
//                    connection = socket.accept();
//                } catch (SocketException socketException) {
//                    System.out.println("closed socket");
//                    return null;
//                } catch (IOException ioException) {
//                    /* TODO */
//                    return null;
//                }
//
//                System.out.println("got a socket");
//                changeToGameScene(stage, connection);
//                return null;
//            }
//        };

    }

    @FXML
    private void connectToServer(Event e){
        String IP;
        int port;

        try {
            port = getPortFromTextField(connectPortText);
        } catch (NumberFormatException numberFormatException) {
            /* TODO dialog to show that the entered port is invalid*/
            System.out.println("Invalid port number");
            return;
        }

        IP = connectIPText.getText();

        System.out.println(IP + ":" + port);
        try {
            connection = new Socket(IP, port);
        } catch (UnknownHostException e1) {
            /* TODO dialog to show that the ip is invalid*/
            System.out.println("Unknown host");
            return;
        } catch (IOException e1) {
            /* TODO dialog to show that the server is not up*/
            System.out.println("Other IOException");
            return;
        }

        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
        changeToGameScene(stage, false);
    }

    private int getPortFromTextField(TextField textField) {
        int port;
        port = Integer.parseInt(textField.getText());
        if (port < 0 || port > 65535)
            throw new NumberFormatException();
        return port;
    }

    private void changeToGameScene(Stage stage, boolean turn){
        changeToGameScene(stage, connection, turn);
    }

    private void changeToGameScene(Stage stage, Socket connection, boolean turn) {

        GameController gc;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameScene.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.out.println("root null");
            e.printStackTrace();
            return;
        }
        stage.setTitle("Tic Tac Toe");
        stage.setScene(new Scene(root, gameWidth, gameHeight));
        stage.show();
        gc = loader.getController();
        gc.initializeSocket(connection);
        gc.focusSendText();
        gc.setMyTurn(turn);

    }

    public void updateHostIPAddress() {
        try {
            hostIPText.setText(new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com/").openStream())).readLine());
        } catch (IOException e) {
            hostIPText.setText("Failed to get IP");
        }

    }
}
