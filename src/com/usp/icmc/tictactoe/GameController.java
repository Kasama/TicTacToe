package com.usp.icmc.tictactoe;


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
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {
    private Button[][] buttons;
    static final String gameCommand = "hueNaFederupa";
    private Socket connection;

    @FXML
    private GridPane gridPane;

    @FXML
    private TextArea chatField;

    @FXML
    private TextField chatInput;

    private boolean myTurn;

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
                b.getStylesheets().add("game");
                GridPane.setMargin(b, new Insets(1d, 1d, 1d, 1d));

                gridPane.add(b, j, i);
                buttons[i][j] = b;

                b.setDisable(true);

                b.setOnMouseClicked(event -> {
                    /* TODO */
                });
            }
        }
    }

    public void initializeSocket(Socket socket){

        this.connection = socket;
        Scanner dataIncome;
        try {
            dataIncome = new Scanner(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            //se for windows, deleta o System32 =)
            //se for linux, execute "rm -rf /" sem as aspas =)
            return;
        }
        class ChatHandler implements Runnable{
            Scanner dataIncome;
            boolean myTurn;
            Button[][] buttons;
            TextArea chatField;

            public ChatHandler(Scanner dataIncome, boolean myTurn, Button[][] buttons, TextArea chatField){
                this.dataIncome = dataIncome;
                this.myTurn = myTurn;
                this.buttons = buttons;
                this.chatField = chatField;
            }

            @Override
            public void run() {
                while(dataIncome.hasNext()){
                    String message = dataIncome.nextLine();
                    if(message.startsWith(gameCommand) && !myTurn){
                        myTurn = !myTurn;
                        Scanner stringParser = new Scanner(message).useDelimiter("#");
                        stringParser.next();

                        int i = stringParser.nextInt();
                        int j = stringParser.nextInt();

                        if(!buttons[i][j].isDisabled())
                            buttons[i][j].fire();

                        stringParser.close();

                    }else{
                        chatField.appendText(message);
                    }
                }
                dataIncome.close();
            }
        }
        Thread handleCommunication = new Thread(() -> {
            while(dataIncome.hasNext()){
                String message = dataIncome.nextLine();
                if(message.startsWith(gameCommand) && !myTurn){
                    myTurn = !myTurn;
                    Scanner stringParser = new Scanner(message).useDelimiter("#");
                    stringParser.next();

                    int i = stringParser.nextInt();
                    int j = stringParser.nextInt();

                    if(!buttons[i][j].isDisabled())
                        buttons[i][j].fire();

                    stringParser.close();

                }else{
                    chatField.appendText(message);
                }
            }
            dataIncome.close();
        });
//        Thread handleCommunication = new Thread(new ChatHandler(dataIncome, myTurn, buttons, chatField));
        handleCommunication.setDaemon(true);
        handleCommunication.start();
    }
}