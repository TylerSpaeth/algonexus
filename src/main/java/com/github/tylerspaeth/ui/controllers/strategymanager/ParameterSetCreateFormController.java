package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.strategy.StrategyManagerService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterSetCreateFormController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSetCreateFormController.class);

    private Strategy strategy;

    private StrategyManagerService strategyManagerService;

    @FXML
    private TextField parameterSetNameTextField;

    @FXML
    private TextField parameterSetDescriptionTextField;

    /**
     * Handles submitting the form. Validates that the required fields are filled and if they are, it submits.
     * Submission of the form will close the modal and cause the data to be entered into the database.
     */
    @FXML
    private void submitForm(ActionEvent actionEvent) {
        if(parameterSetNameTextField.getText().isBlank()) {
            LOGGER.warn("Unable to submit form, set name is required.");
            return;
        }

        LOGGER.info("Submitting form for parameter set: {}", parameterSetNameTextField.getText());

        boolean uploadSuccess = strategyManagerService.addNewParameterSetToExistingStrategy(strategy, parameterSetNameTextField.getText(), parameterSetDescriptionTextField.getText());

        // Modal will close if upload is successful, otherwise it stays open
        if(uploadSuccess) {
            LOGGER.info("Successfully created parameter set: {}", parameterSetNameTextField.getText());
            ((Stage) ((Node)actionEvent.getSource()).getScene().getWindow()).close();
        }
        else {
            LOGGER.error("Failed to create parameter set: {}", parameterSetNameTextField.getText());
        }
    }

    public void setStrategyManagerService(StrategyManagerService strategyManagerService) {
        this.strategyManagerService = strategyManagerService;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
