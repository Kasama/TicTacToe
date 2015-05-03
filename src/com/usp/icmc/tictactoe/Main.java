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
        // Loads the first Scene.
        // This scene shows a host and a connect field so you can either
        // host a game or connect to someone hosting it
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HostConnectScene.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Tic Tac Toe Menu");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        // Updates a text field to have your external IP address, so its easier
        // for you to tell someone to connect to you
        loader.<HostConnectController>getController().updateHostIPAddress();
        // Set the platform to exit when the window is closed
        primaryStage.setOnCloseRequest(event -> Platform.exit());
    }


    // Launches the JavaFX Application
    public static void main(String[] args) {
        launch(args);
    }
}
