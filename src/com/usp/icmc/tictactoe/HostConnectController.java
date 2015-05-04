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

    // constants
    private final double gameWidth = 800;
    private final double gameHeight = 600;
    private final long timeout = 6000;

    // Screen objects
    @FXML private RadioButton ORadio;
    @FXML private RadioButton XRadio;
    @FXML private TextField nicknameField;
    @FXML private ToggleGroup selectRadio;
    @FXML private ToggleGroup iconRadio;
    @FXML private TextField hostPortText;
    @FXML private TextField hostIPText;
    @FXML private TextField connectIPText;
    @FXML private TextField connectPortText;
    @FXML private RadioButton hostRadio;
    @FXML private RadioButton connectRadio;
    @FXML private Button hostButton;
    @FXML private Button connectButton;
    @FXML private ProgressIndicator hostIndicator;

    // The connection socket
    private Socket connection;

    @FXML // When the radio button to host is clicked, disable the connect area
    private void setRadioHost(Event e) {
        connectIPText.setDisable(true);
        connectPortText.setDisable(true);
        connectButton.setDisable(true);
        hostPortText.setDisable(false);
        hostButton.setDisable(false);
    }

    @FXML // When the radio button to connect is clicked, disable the host area
    private void setRadioConnect(Event e) {
        connectIPText.setDisable(false);
        connectPortText.setDisable(false);
        connectButton.setDisable(false);
        hostPortText.setDisable(true);
        hostButton.setDisable(true);
    }

    @FXML // Host button was clicked, open a connection
    private void waitForConnection(Event e) {

        int port;
        final ServerSocket socket;

        try {
            // tries to parse a port number from the text field
            port = getPortFromTextField(hostPortText);
        } catch (NumberFormatException numberFormatException) {
            // if the text field contains something other than a port number
            // show a dialog
            showDialog(
                    "Invalid Port",
                    "Invalid port number: " + hostPortText.getText(),
                    Alert.AlertType.WARNING, ButtonType.OK
            );
            return;
        }


        try {
            // tries to open a server socket on given port
            socket = new ServerSocket(port);
        } catch (IOException ioException) {
            // if it was not possible, show a dialog
            // most of the reasons why this would fail is if a port is in use
            showDialog(
                    "Could not open port",
                    "Could not open port: " + port + ", maybe it's in use",
                    Alert.AlertType.ERROR, ButtonType.OK
            );
            System.err.println("Could not open port " + port + ". Maybe it's in use");
            return;
        }

        // creates a timer thread to kill the socket after a timeout
        Timer timer = new Timer();
        Thread handleServerAcceptanceTimeout = new Thread(
                () -> {
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        // kills the socket
                                        // will cause a SocketException to be
                                        // thrown at the other thread
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

        // shows a indicator that the server is waiting for a connection
        hostIndicator.setVisible(true);
        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());
        // thread to wait until someone connects
        // A thread is needed, otherwise the GUI would freeze
        Thread handleServerAcceptance = new Thread(
                () -> {
                    try {
                        // wait for a connection. This stops the thread until
                        // something happens
                        connection = socket.accept();
                    } catch (SocketException socketException) {
                        // if the socket times out due to the previous thread
                        // show a dialog and do nothing else
                        Platform.runLater(
                                () -> showDialog(
                                        "Connection Timeout",
                                        "Your connection timed out, click 'host' again",
                                        Alert.AlertType.INFORMATION,
                                        ButtonType.OK
                                )
                        );
                        return;
                    } catch (IOException ioException) {
                        // this should not happen under normal conditions
                        Platform.runLater(
                                () -> showDialog(
                                        "hosting problem",
                                        "There was an error while trying to host a connection",
                                        Alert.AlertType.ERROR, ButtonType.OK
                                )
                        );
                        return;
                    } finally {
                        // kills the timeout thread.
                        // fixes a bug when the thread would remain alive after
                        // the program closed
                        timer.cancel();
                        handleServerAcceptanceTimeout.interrupt();
                    }

                    // change to the game scene, because the connection was
                    // successful
                    Platform.runLater(
                            () -> changeToGameScene(stage, connection, true)
                    );
                }
        );
        handleServerAcceptance.setDaemon(true);
        handleServerAcceptance.start();

    }

    // A simple function that creates a dialog box using the Alert class
    private void showDialog(
            String title, String message, Alert.AlertType type,
            ButtonType buttonType
    ) {
        Alert alert = new Alert(type, message, buttonType);
        alert.getDialogPane().getStylesheets().add("/res/gameStyle.css");
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    @FXML // Connect button was clicked, try to connect to a host
    private void connectToServer(Event e) {
        String IP;
        int port;

        try {
            // tries to parse a port number from the text field
            port = getPortFromTextField(connectPortText);
        } catch (NumberFormatException numberFormatException) {
            // if the text field contains something other than a port number
            // show a dialog
            showDialog(
                    "Invalid Port",
                    "Invalid port number: " + connectPortText.getText(),
                    Alert.AlertType.WARNING, ButtonType.OK
            );
            return;
        }

        IP = connectIPText.getText();

        try {
            // tried to connect to IP:port
            connection = new Socket(IP, port);
        } catch (UnknownHostException e1) {
            // show a dialog for unreachable host
            // most cases the IP is wrong, or there is no internet connection
            showDialog(
                    "Unknown host",
                    "Couldn't find route to host " + connectIPText.getText(),
                    Alert.AlertType.ERROR, ButtonType.OK
            );
            return;
        } catch (IOException e1) {
            // show a dialog for wrong port
            // it was possible to contact the IP, but the port is not open
            // maybe the typed port is wrong or the host timed out
            showDialog(
                    "Host not up",
                    "Host "+connectIPText.getText()+" is not up, maybe the port is wrong",
                    Alert.AlertType.ERROR, ButtonType.OK
            );
            return;
        }

        Stage stage = ((Stage) ((Button) e.getSource()).getScene().getWindow());

        changeToGameScene(stage, false);
    }

    // A simple function to extract a port number from a TextField
    // the text must be a positive number, less than 65536
    private int getPortFromTextField(TextField textField) {
        int port;
        port = Integer.parseInt(textField.getText());
        if (port < 0 || port > 65535)
            throw new NumberFormatException();
        return port;
    }

    // A simple function to change the game scene to the game once a connection
    // was made
    private void changeToGameScene(Stage stage, boolean turn) {
        changeToGameScene(stage, connection, turn);
    }

    // A simple function to change the game scene to the game once a connection
    // was made
    private void changeToGameScene(
            Stage stage, Socket connection, boolean turn
    ) {

        GameController gc;
        // get a scene loader for the next scene
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("gameScene.fxml")
        );
        Parent root;
        try {
            // tries to load it
            root = loader.load();
        } catch (IOException e) {
            // this should not happen
            e.printStackTrace();
            return;
        }
        // setup the new scene and arrange the stage for it
        stage.setScene(new Scene(root));
        stage.setHeight(gameHeight);
        stage.setWidth(gameWidth);
        stage.setTitle("Tic Tac Toe");
        stage.show();
        // get a scene controller reference to pass the connection socket to it
        // and focus a text field
        gc = loader.getController();
        gc.initializeSocketReader(connection);
        gc.focusSendText();
        // Host always goes first and client goes second
        gc.setTurn(turn);
        gc.setIcon(((RadioButton) iconRadio.getSelectedToggle()).getText());
        gc.setNickName(nicknameField.getText());
        gc.printWelcomeText();

    }

    // Function that checks the users external IP address and places it into a
    // text field
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