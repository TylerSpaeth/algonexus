package com.github.tylerspaeth;

import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.ui.GUI;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        DatasourceConfig.validate();

        Application.launch(GUI.class, "");

    }
}