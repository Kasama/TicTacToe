package com.usp.icmc.tictactoe;

import java.net.Socket;

public class ConnectionController {

    private static Socket connection;

    public static Socket getConnection() {
        return connection;
    }

    public static void setConnection(Socket connection) {
        ConnectionController.connection = connection;
    }
}
