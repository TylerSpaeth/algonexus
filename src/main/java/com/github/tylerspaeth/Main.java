package com.github.tylerspaeth;

import com.github.tylerspaeth.config.IBConfig;
import com.github.tylerspaeth.ui.GUI;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        IBConfig config = new IBConfig();
        config.connect();

        Application.launch(GUI.class, "");

        config.disconnect();

    }
}