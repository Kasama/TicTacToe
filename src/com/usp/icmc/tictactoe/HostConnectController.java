package com.usp.icmc.tictactoe;

import javafx.application.Platform;
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

    private final double gameWidth = 800;
    private final double gameHeight = 600;
    private final long timeout = 6000;

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

    private Socket connection;

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

        try {
            port = getPortFromTextField(hostPortText);
        } catch (NumberFormatException numberFormatException) {
            showDialog(
                    "Invalid Port", "Warning:",
                    "Invalid port number: " + hostPortText.getText(),
                    Alert.AlertType.WARNING, ButtonType.OK
            );
            System.err.println("Invalid port number");
            return;
        }

        try {
            socket = new ServerSocket(port);
        } catch (IOException ioException) {
            showDialog(
                    "Could not open port", "Error:",
                    "Could not open port: " + port + ", maybe it's in use",
                    Alert.AlertType.ERROR, ButtonType.OK
            );
            System.err.println("Could not open port " + port);
            return;
        }

        Timer timer = new Timer();
        Thread handleServerAcceptanceTimeout = new Thread(
                () -> {
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        socket.close();
                                        hostIndicator.setVisible(false);
                                    } catch (IOException ioException) {
                                        System.err.println(
                                                "Not possible to close the socket"
                                        );
                                    }
                                }
                            }, timeout
                    );
                }
        );
        handleServerAcceptanceTimeout.setDaemon(true);
        handleServerAcceptanceTimeout.start();

        hostIndicator.setVisible(true);
        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
        Thread handleServerAcceptance = new Thread(
                () -> {
                    try {
                        connection = socket.accept();
                    } catch (SocketException socketException) {
                        Platform.runLater(
                                () -> showDialog(
                                        "Connection Timeout", "Information",
                                        "Your connection timed out, click 'host' again",
                                        Alert.AlertType.INFORMATION,
                                        ButtonType.OK
                                )
                        );
                        return;
                    } catch (IOException ioException) {
                        Platform.runLater(
                                () -> showDialog(
                                        "hosting problem", "Error",
                                        "There was an error while trying to host a connection",
                                        Alert.AlertType.ERROR, ButtonType.OK
                                )
                        );
                        return;
                    } finally {
                        timer.cancel();
                        handleServerAcceptanceTimeout.interrupt();
                    }

                    Platform.runLater(
                            () -> changeToGameScene(stage, connection, true)
                    );
                }
        );
        handleServerAcceptance.setDaemon(true);
        handleServerAcceptance.start();

    }

    private void showDialog(
            String title, String header, String message, Alert.AlertType type,
            ButtonType buttonType
    ) {
        Alert alert = new Alert(type, message, buttonType);
        alert.getDialogPane().getStylesheets().add("/res/gameStyle.css");
        alert.setHeaderText(header);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML
    private void connectToServer(Event e) {
        String IP;
        int port;

        try {
            port = getPortFromTextField(connectPortText);
        } catch (NumberFormatException numberFormatException) {
            showDialog(
                    "Invalid Port", "Warning:",
                    "Invalid port number: " + hostPortText.getText(),
                    Alert.AlertType.WARNING, ButtonType.OK
            );
            System.out.println("Invalid port number");
            return;
        }

        IP = connectIPText.getText();

        try {
            connection = new Socket(IP, port);
        } catch (UnknownHostException e1) {
            showDialog(
                    "Unknown host", "Error:",
                    "Couldn't find rout to host " + connectIPText.getText(),
                    Alert.AlertType.ERROR, ButtonType.OK
            );
            return;
        } catch (IOException e1) {
            showDialog(
                    "Host not up", "Error:",
                    "Host "+connectIPText.getText()+" is not up, maybe the port is wrong",
                    Alert.AlertType.ERROR, ButtonType.OK
            );
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

    private void changeToGameScene(Stage stage, boolean turn) {
        changeToGameScene(stage, connection, turn);
    }

    private void changeToGameScene(
            Stage stage, Socket connection, boolean turn
    ) {

        GameController gc;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("gameScene.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
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
            hostIPText.setText(
                new BufferedReader(
                    new InputStreamReader(
                        new URL("http://checkip.amazonaws.com/") .openStream()
                    )
                ).readLine()
            );
        } catch (IOException e) {
            hostIPText.setText("Failed to get IP");
        }

    }
}