package com.github.tylerspaeth.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    @FXML
    public BorderPane borderPane;

    @FXML
    public Button accountButton;
    private static final String ACCOUNT_FXML = "/com/github/tylerspaeth/fxml/Account.fxml";

    @FXML
    public Button strategiesButton;
    private static final String STRATEGY_MANANGER_FXML = "/com/github/tylerspaeth/fxml/strategymanager/StrategyManager.fxml";

    @FXML
    public Button homeButton;
    private static final String HOME_FXML = "/com/github/tylerspaeth/fxml/Home.fxml";

    @FXML
    public Button dataManagerButton;
    private static final String DATA_MANAGER_FXML = "/com/github/tylerspaeth/fxml/datamanager/DataManager.fxml";

    /**
     * Switches what is in the center of the border pane to the provided FXML path.
     * @param fxmlPath Path to FXML file that should be loaded
     */
    private void switchContext(String fxmlPath) {
        try {
            // Load the content FXML file
            Parent content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            // Set the loaded content into the center of the BorderPane
            borderPane.setCenter(content);
        } catch (IOException e) {
            LOGGER.error("Failed to switch context", e);
            // Handle the exception, e.g., show an error message
        }
    }

    /**
     * Switches to the home screen
     */
    @FXML
    public void homeButtonClicked() {
        switchContext(HOME_FXML);
    }

    /**
     * Switches to the account screen
     */
    @FXML
    public void accountButtonClicked() {
        switchContext(ACCOUNT_FXML);
    }

    /**
     * Switches to the strategy manager screen
     */
    @FXML
    public void strategiesButtonClicked() {
        switchContext(STRATEGY_MANANGER_FXML);
    }

    /**
     * Switches to the data manager screen
     */
    @FXML
    public void dataManagerButtonClicked() {
        switchContext(DATA_MANAGER_FXML);
    }

}
