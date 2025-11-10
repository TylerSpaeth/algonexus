package com.github.tylerspaeth.ui;

import com.github.tylerspaeth.util.AlgoNexusConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GUI extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/github/tylerspaeth/fxml/Main.fxml")));
        stage.setTitle(AlgoNexusConstants.ALGO_NEXUS);
        stage.setScene(new Scene(root, AlgoNexusConstants.INITIAL_WIDTH, AlgoNexusConstants.INITIAL_HEIGHT));
        stage.show();
    }

}
