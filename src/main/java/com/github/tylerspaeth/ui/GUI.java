package com.github.tylerspaeth.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GUI extends Application {

    public static final String ALGO_NEXUS = "AlgoNexus";
    public static final int INITIAL_WIDTH = 960;
    public static final int INITIAL_HEIGHT = 540;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/github/tylerspaeth/fxml/Main.fxml")));
        stage.setTitle(ALGO_NEXUS);
        stage.setScene(new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT));
        stage.show();
    }

}
