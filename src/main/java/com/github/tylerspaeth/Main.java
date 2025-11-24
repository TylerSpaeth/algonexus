package com.github.tylerspaeth;

import com.github.tylerspaeth.ib.IBConnection;
import com.github.tylerspaeth.ui.GUI;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        IBConnection connection = new IBConnection();
        connection.connect();

        Application.launch(GUI.class, "");

        connection.disconnect();

    }
}