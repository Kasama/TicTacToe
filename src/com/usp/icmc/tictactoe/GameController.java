package com.usp.icmc.tictactoe;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {
    static final String gameCommand
            = "casoiehcsliuaseavsnufhaiushvnfoisduhacnshfnijsbhidj";
    static final String XStyle = "buttonPressedX";
    static final String OStyle = "buttonPressedO";
    private Button[][] buttons;
    private Socket connection;

    @FXML private GridPane gridPane;
    @FXML private TextArea chatField;
    @FXML private TextField chatInput;

    private boolean myTurn;
    private boolean turn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int width, height;

        width = gridPane.getColumnConstraints().size();
        height = gridPane.getRowConstraints().size();

        buttons = new Button[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Button b = new Button();

                b.setAlignment(Pos.CENTER);
                b.setMaxHeight(Double.MAX_VALUE);
                b.setMaxWidth(Double.MAX_VALUE);
                b.getStyleClass().add("gameButton");
                GridPane.setMargin(b, new Insets(5d, 5d, 5d, 5d));

                gridPane.add(b, j, i);
                buttons[i][j] = b;

                b.setOnAction(new buttonHandler());
            }
        }
    }

    class buttonHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            if (!myTurn)
                return;
            PrintWriter writer;
            try {
                writer = new PrintWriter(connection.getOutputStream(), true);
            } catch (IOException e) {
                return;
            }
            myTurn = !myTurn;
            Button button = ((Button) event.getSource());
            button.setDisable(true);
            button.getStyleClass().add(getStyle(turn));
            if (checkGameOver()) {
                for (Button[] button1 : buttons)
                    for (Button button2 : button1)
                        button2.setDisable(true);
                if (turn) {
                    chatField.appendText("You Won!\n");
                } else {
                    chatField.appendText("You Lost!\n");
                }
            }
            if (!turn) {
                myTurn = !myTurn;
                turn = true;
                return;
            }
            turn = !turn;
            writer.println(gameCommand);

            int x, y = 0;
            All:
            for (x = 0; x < buttons.length; x++) {
                for (y = 0; y < buttons.length; y++) {
                    if (buttons[x][y].equals(button))
                        break All;
                }
            }
            writer.println(x);
            //noinspection SuspiciousNameCombination
            writer.println(y);

            focusSendText();
        }
    }

    @FXML
    private void handleEnter(Event e) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(connection.getOutputStream(), true);
        } catch (IOException e1) {
            return;
        }
        if (chatInput.getText().equals(""))
            return;
        writer.println(chatInput.getText());
        chatField.appendText(chatInput.getText() + "\n");
        chatInput.setText("");
    }

    private String getStyle(boolean turn) {
        return turn ? OStyle : XStyle;
    }

    private boolean checkGameOver() {
        // test for each row to X
        for (int i = 0; i < buttons.length; i++) {
            if (
                    buttons[i][0].getStyleClass().contains(OStyle) &&
                    buttons[i][1].getStyleClass().contains(OStyle) &&
                    buttons[i][2].getStyleClass().contains(OStyle)
                    )
                return true;
            else if (
                    buttons[0][i].getStyleClass().contains(OStyle) &&
                    buttons[1][i].getStyleClass().contains(OStyle) &&
                    buttons[2][i].getStyleClass().contains(OStyle)
                    )
                return true;
        }
        // test for each row to O
        for (int i = 0; i < buttons.length; i++) {
            if (
                    buttons[i][0].getStyleClass().contains(XStyle) &&
                    buttons[i][1].getStyleClass().contains(XStyle) &&
                    buttons[i][2].getStyleClass().contains(XStyle)
                    )
                return true;
            else if (
                    buttons[0][i].getStyleClass().contains(XStyle) &&
                    buttons[1][i].getStyleClass().contains(XStyle) &&
                    buttons[2][i].getStyleClass().contains(XStyle)
                    )
                return true;
        }

        if (
                buttons[0][0].getStyleClass().contains(OStyle) &&
                buttons[1][1].getStyleClass().contains(OStyle) &&
                buttons[2][2].getStyleClass().contains(OStyle)
                )
            return true;
        if (
                buttons[0][0].getStyleClass().contains(XStyle) &&
                buttons[1][1].getStyleClass().contains(XStyle) &&
                buttons[2][2].getStyleClass().contains(XStyle)
                )
            return true;

        if (
                buttons[0][2].getStyleClass().contains(OStyle) &&
                buttons[1][1].getStyleClass().contains(OStyle) &&
                buttons[2][0].getStyleClass().contains(OStyle)
                )
            return true;

        if (
                buttons[0][2].getStyleClass().contains(XStyle) &&
                buttons[1][1].getStyleClass().contains(XStyle) &&
                buttons[2][0].getStyleClass().contains(XStyle)
                )
            return true;

        return false;
    }

    public void initializeSocket(Socket socket) {

        this.connection = socket;
        Scanner dataIncome;
        try {
            dataIncome = new Scanner(connection.getInputStream());
        } catch (IOException e) {
            System.err.println("Could not get connection socket");
            return;
        }
        Thread handleCommunication = new Thread(
                () -> {
                    while (dataIncome.hasNext()) {
                        String message = dataIncome.nextLine();
                        if (message.startsWith(gameCommand) && !myTurn) {
                            int i = dataIncome.nextInt();
                            int j = dataIncome.nextInt();

                            if (!(buttons[i][j].isDisable()))
                                myTurn = !myTurn;
                            buttons[i][j].fire();
                        } else {
                            chatField.appendText(message + "\n");
                        }
                    }
                    dataIncome.close();
                }
        );
        handleCommunication.setDaemon(true);
        handleCommunication.start();
    }

    public void setMyTurn(boolean turn) {
        myTurn = turn;
        this.turn = turn;
    }

    public void focusSendText() {
        try {
            chatInput.requestFocus();
        } catch (Exception ignored) {
        }
    }

}