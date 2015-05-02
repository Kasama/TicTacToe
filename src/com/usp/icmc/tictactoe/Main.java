package com.usp.icmc.tictactoe;

import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("gameScene.fxml"));
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
