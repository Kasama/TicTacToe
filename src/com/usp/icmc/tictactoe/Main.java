package com.usp.icmc.tictactoe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HostConnectScene.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Tic Tac Toe Menu");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        loader.<HostConnectController>getController().updateHostIPAddress();
        primaryStage.setOnCloseRequest(event -> Platform.exit());
    }


    public static void main(String[] args) {
        launch(args);
    }
}
