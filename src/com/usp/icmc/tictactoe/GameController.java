package com.usp.icmc.tictactoe;


import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {
    private Button[][] buttons;
    static final String gameCommand = "command";
    private Socket connection;

    @FXML
    private GridPane gridPane;

    @FXML
    private TextArea chatField;

    @FXML
    private TextField chatInput;

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
                GridPane.setMargin(b, new Insets(1d, 1d, 1d, 1d));

                gridPane.add(b, j, i);
                buttons[i][j] = b;

                b.setOnAction(event -> {
                    if (!myTurn)
                        return;
                    PrintWriter writer = null;
                    try {
                        writer = new PrintWriter(connection.getOutputStream(), true);
                    } catch (IOException e) {
                        return;
                    }
                    myTurn = !myTurn;
                    Button button = ((Button) event.getSource());
                    button.setDisable(true);
                    button.getStyleClass().add(getStyle(turn));
                    if(!turn) {
                        myTurn = !myTurn;
                        turn = true;
                        return;
                    }
                    turn = !turn;
                    writer.println(gameCommand);

                    int x = 0, y = 0;
                    All:
                    for (x = 0; x < buttons.length ; x++) {
                        for (y = 0; y < buttons.length; y++) {
                            if(buttons[x][y].equals(button))
                                break All;
                        }
                    }
                    writer.println(x);
                    writer.println(y);
//                    writer.flush();


                    if (checkGameOver()) {
                        if (myTurn) {
                            /* TODO I won! */
                            System.out.println("wee");
                        } else {
                            /* TODO opponent won */
                            System.out.println("ahh");
                        }
                    }
                    focusSendText();
                });
            }
        }
    }

    private String getStyle(boolean turn) {
        /* TODO add a nice manager to styles */
        return turn?"buttonPressedO":"buttonPressedX";
    }

    @FXML
    private void handleEnter (Event e){
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

    private boolean checkGameOver() {
        /* TODO check if the game is over */
        return false;
    }

    public void initializeSocket(Socket socket){

        this.connection = socket;
        Scanner dataIncome;
        try {
            dataIncome = new Scanner(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            /* TODO handle exception gracefully */
            return;
        }
        Thread handleCommunication = new Thread(() -> {
            while(dataIncome.hasNext()){
                String message = dataIncome.nextLine();
                if(message.startsWith(gameCommand) && !myTurn){
                    int i = dataIncome.nextInt();
                    int j = dataIncome.nextInt();

                    if(!(buttons[i][j].isDisable()))
                        myTurn = !myTurn;
                    buttons[i][j].fire();
                }else{
                    chatField.appendText(message + "\n");
                }
            }
            dataIncome.close();
        });
        handleCommunication.setDaemon(true);
        handleCommunication.start();
    }

    public void setMyTurn(boolean turn){
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