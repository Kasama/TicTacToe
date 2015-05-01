package com.usp.icmc.tictactoe;

import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PopUpDialogBox {

    static final int MESSAGE_DEFAULT = 0;
    static final int MESSAGE_INFORMATION = 1;
    static final int MESSAGE_QUESTION = 2;
    static final int MESSAGE_ALERT = 3;

    static final int BUTTONS_OK = 0;
    static final int BUTTONS_OK_CANCEL = 1;
    static final int BUTTONS_YES_NO_CANCEL = 2;

    static final int SELECTED_OK = 0;
    static final int SELECTED_CANCEL = 1;
    static final int SELECTED_YES = 2;
    static final int SELECTED_NO = 3;

    private Stage stage;
    private String displayMessage;
    private int messageType;
    private int buttons;

    public PopUpDialogBox(int messageType, String displayMessage, int buttons) {
        stage = new Stage();

        Parent p = new VBox();


    }

    static int displayDialogBox(String displayMessage){
        return displayDialogBox(MESSAGE_DEFAULT, displayMessage, BUTTONS_OK);
    }

    static int displayDialogBox(String displayMessage, int buttons){
        return displayDialogBox(MESSAGE_DEFAULT, displayMessage, buttons);
    }

    static int displayDialogBox(int messageType, String displayMessage, int buttons){

        PopUpDialogBox dialog = new PopUpDialogBox(messageType, displayMessage, buttons);

        return SELECTED_OK;
    }


}
