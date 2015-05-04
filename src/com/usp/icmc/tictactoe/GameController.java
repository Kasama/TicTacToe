package com.usp.icmc.tictactoe;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class GameController implements Initializable {
    // Constants
    static final String gameCommand
            = "casoiehcsliuaseavsnufhaiushvnfoisduhacnshfnijsbhidj";
    static final String XStyle = "buttonPressedX";
    static final String OStyle = "buttonPressedO";

    // 3x3 grid containing all buttons
    private Button[][] buttons;
    // The connection socket received from the previous scene
    private Socket connection;

    // Screen objects
    @FXML private GridPane gridPane;
    @FXML private TextArea chatField;
    @FXML private TextField chatInput;

    // Game variables
    private boolean canClick;
    private boolean turn;
    private boolean gameOver = false;
    private String myStyle;
    private String opponentStyle;
    private String nickName;

    @Override // Method ran upon load. Used to generate the buttons
    public void initialize(URL location, ResourceBundle resources) {
        int width, height;

        width = gridPane.getColumnConstraints().size();
        height = gridPane.getRowConstraints().size();

        buttons = new Button[width][height];

        // generates the buttons
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Button b = new Button();

                b.setAlignment(Pos.CENTER);
                b.setMaxHeight(Double.MAX_VALUE);
                b.setMaxWidth(Double.MAX_VALUE);
                // style them
                b.getStyleClass().add("gameButton");
                GridPane.setMargin(b, new Insets(5d, 5d, 5d, 5d));

                gridPane.add(b, j, i);
                // save a button reference in a matrix
                buttons[i][j] = b;

                // adds a event handler
                b.setOnAction(new buttonHandler());
            }
        }
    }

    // Gets the style to use from previous scene
    public void setIcon(String icon) {
        if (icon.startsWith("X")){
            this.myStyle = XStyle;
            this.opponentStyle = OStyle;
        }else{
            this.myStyle = OStyle;
            this.opponentStyle = XStyle;
        }
    }

    // Gets the nickname, from previous scene
    public void setNickName(String nickName) {
        if(nickName.equals(""))
            this.nickName = turn ? "adenilsoN 1" : "adenilsoN 2";
        else
            this.nickName = nickName;
    }

    public void printWelcomeText() {
        chatField.appendText(
                "[Game] " + nickName + ", welcome to the Tic Tac Toe game!\n"
        );
        chatField.appendText("[Game] You can chat with your opponent here!\n");
        if(turn){
            chatField.appendText("[Game] It's your turn!\n\n");
        }else{
            chatField.appendText("[Game] It's your opponent's turn!\n\n");
        }
    }

    // Inner class to handle the button presses
    class buttonHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            // request focus to the chat for a better experience
            focusSendText();
            // check if the button can be clicked (by the player or fire() method)
            // it can if it's his turn or if it received a chat command from the other player
            if (!canClick)
                return;
            PrintWriter writer;
            try {
                // tries to get the output stream. used to send a command to
                // the other player
                writer = new PrintWriter(connection.getOutputStream(), true);
            } catch (IOException e) {
                // this exception should only happen if one player loses connection
                return;
            }
            // toggles the canClick state
            canClick = !canClick;
            Button button = ((Button) event.getSource());
            button.setDisable(true);
            // style the button to show an X or a O, depending on turn
            button.getStyleClass().add(getStyle(turn));
            // checks if the game is gameOver and disables the board if so
            if (checkGameOver()) {
                for (Button[] button1 : buttons)
                    for (Button button2 : button1)
                        button2.setDisable(true);
                // also prints a chat message to say if the player won or lost
                if (turn) {
                    chatField.appendText("[Game] You Won!\n");
                } else {
                    chatField.appendText("[Game] You Lost!\n");
                }
                gameOver = true;
            }else if(isBoardFull()){
                chatField.appendText("[Game] Draw!\n");
                gameOver = true;
            }
            // if this event happened due to a chat command. end it here
            if (!turn) {
                // set the turn before leaving
                canClick = !canClick;
                turn = true;
                return;
            }
            // toggle turn
            turn = !turn;
            // send a chat command to the other user
            writer.println(gameCommand);

            int x, y = 0;
            // finds out which button was clicked
            All:
            for (x = 0; x < buttons.length; x++) {
                for (y = 0; y < buttons.length; y++) {
                    if (buttons[x][y].equals(button))
                        break All;
                }
            }
            // sends a x and y value
            writer.println(x);
            writer.println(y);

            if(gameOver)
                checkPlayAgain();
        }
    }

    private void checkPlayAgain() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you wanna play again?",
                                ButtonType.YES, ButtonType.NO);
        alert.getDialogPane().getStylesheets().add("/res/gameStyle.css");
        alert.setTitle("Play Again?");
        alert.show();
        if(alert.getResult().equals(ButtonType.YES)){
            //prey again
        }else{
            //fuck u
        }
    }

    // Method that checks if the board is full
    private boolean isBoardFull() {
        for(Button[] button : buttons) {
            for (Button b : button)
                if (!b.isDisable()) return false;
        }
        return true;
    }

    @FXML // Method called when enter is pressed on the chatInput field
    private void handleEnter(Event e) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(connection.getOutputStream(), true);
        } catch (IOException e1) {
            return;
        }
        if (chatInput.getText().equals(""))
            return;
        // append text to own chat and send it to opponent
        writer.println(nickName+": " + chatInput.getText());
        chatField.appendText("You: " + chatInput.getText() + "\n");
        chatInput.setText("");
    }

    // Returns the style class to use based on turn
    private String getStyle(boolean turn) {
        return turn ? myStyle : opponentStyle;
    }

    // Function to check every game gameOver scenario
    private boolean checkGameOver() {
        for (int i = 0; i < buttons.length; i++) {
            // test for each row to O
            if (
                    buttons[i][0].getStyleClass().contains(OStyle) &&
                    buttons[i][1].getStyleClass().contains(OStyle) &&
                    buttons[i][2].getStyleClass().contains(OStyle)
                    )
                return true;
            // test for each column to O
            else if (
                    buttons[0][i].getStyleClass().contains(OStyle) &&
                    buttons[1][i].getStyleClass().contains(OStyle) &&
                    buttons[2][i].getStyleClass().contains(OStyle)
                    )
                return true;
        }
        for (int i = 0; i < buttons.length; i++) {
            // test for each row to X
            if (
                    buttons[i][0].getStyleClass().contains(XStyle) &&
                    buttons[i][1].getStyleClass().contains(XStyle) &&
                    buttons[i][2].getStyleClass().contains(XStyle)
                    )
                return true;
            // test for each column to X
            else if (
                    buttons[0][i].getStyleClass().contains(XStyle) &&
                    buttons[1][i].getStyleClass().contains(XStyle) &&
                    buttons[2][i].getStyleClass().contains(XStyle)
                    )
                return true;
        }

        // check the first diagonal
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

        // check the second diagonal
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

    // Initialize the socket reader
    // this is called from the previous scene to pass the connection socket
    public void initializeSocketReader(Socket socket) {

        this.connection = socket;
        Scanner dataIncome;
        try {
            // wrap the InputStream with a Scanner using \n as the delimiter
            dataIncome = new Scanner(connection.getInputStream()).useDelimiter("\n");
        } catch (IOException e) {
            // this should not happen
            System.err.println("Could not get connection socket");
            return;
        }
        // handle the communication on the background
        Thread handleCommunication = new Thread(
                () -> {
                    // the break condition is when a read is not possible
                    while (true) {
                        String message;
                        try {
                            // tries to read the inputStream
                            message = dataIncome.next();
                        }catch (Exception ignored){
                            // if it can't, the connection must have been dropped
                            break;
                        }
                        // check if the incoming message is a chat command
                        if (message.startsWith(gameCommand) && !canClick) {
                            // reads which button to press
                            int i = dataIncome.nextInt();
                            int j = dataIncome.nextInt();

                            // prevents command injection to play twice in a row
                            if (!(buttons[i][j].isDisable()))
                                canClick = !canClick;
                            // fires the button referenced by the chat command
                            buttons[i][j].fire();
                        } else {
                            // if its not, print it as text
                            chatField.appendText(message + "\n");
                        }
                    }
                    dataIncome.close();
                }
        );
        handleCommunication.setDaemon(true);
        handleCommunication.start();
    }

    // Initialize the turn and CanClick variables
    // this is called from the previous scene to pass the turn
    public void setTurn(boolean turn) {
        canClick = turn;
        this.turn = turn;
    }

    // request focus to the chat input. Used for better experience
    public void focusSendText() {
        try {
            chatInput.requestFocus();
        } catch (Exception ignored) {
        }
    }

}