package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.strategy.StrategyManagerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

public class StrategyDetailsController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyDetailsController.class);

    private static final String PARAMETER_SET_DETAILS_FXML = "/com/github/tylerspaeth/fxml/strategymanager/ParameterSetDetails.fxml";
    private static final String DESCRIPTION = "Description: {0}";
    private static final String PARENT_NAME = "Parent Name: {0}";
    private static final String CREATE_MODAL_TITLE = "Create Strategy Parameter Set";
    private static final int CREATE_MODAL_WIDTH = 500;
    private static final int CREATE_MODAL_HEIGHT = 500;

    private Strategy strategy;

    private StrategyManagerController strategyManagerController;

    private StrategyManagerService strategyManagerService;

    @FXML
    private ListView<StrategyParameterSet> parameterSetListView;
    private final ObservableList<StrategyParameterSet> parameterSets = FXCollections.observableArrayList();
    private StrategyParameterSet selectedParameterSet = null;

    @FXML
    private Text descriptionText;

    @FXML
    private Text parentNameText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(strategy != null) {
            parameterSets.addAll(strategy.getStrategyParameterSets());
        }
        parameterSetListView.setItems(parameterSets);
        parameterSetListView.getSelectionModel().selectedItemProperty().addListener((_obs, _oldVal, newVal) -> {
            selectedParameterSet = newVal;
        });

        setTextFields();
    }

    /**
     * Creates a new StrategyParameterSet and opens it in a new tab.
     */
    @FXML
    private void showCreateParameterSetForm(ActionEvent actionEvent) {
        Stage primaryStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(primaryStage);
        modalStage.setTitle(CREATE_MODAL_TITLE);

        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/github/tylerspaeth/fxml/strategymanager/ParameterSetCreateForm.fxml")));
            Parent modal = loader.load();
            Scene modalScene = new Scene(modal, CREATE_MODAL_WIDTH, CREATE_MODAL_HEIGHT);
            modalStage.setScene(modalScene);

            ParameterSetCreateFormController controller = loader.getController();
            controller.setStrategy(strategy);
            controller.setStrategyManagerService(strategyManagerService);
        } catch (IOException e) {
            LOGGER.error("Failed to load create modal.");
        }

        modalStage.showAndWait();
        strategy = strategyManagerService.refreshStrategy(strategy);
        refreshListView();
    }

    /**
     * Attempts to view the selected StrategyParameterSet by opening it in a new tab.
     */
    @FXML
    private void viewParameterSet() {
        if(selectedParameterSet == null) {
            LOGGER.warn("Unable to show details as no ParameterSet is selected.");
            return;
        }
        openParameterSetDetailsTab(selectedParameterSet);
    }

    /**
     * Refreshes the ListView of StrategyParameterSets by requerying the database.
     */
    @FXML
    private void refreshListView() {
        if(strategy != null) {
            setStrategy(strategy);
        }
    }

    /**
     * Sets the Strategy that the page should be showing details for.
     * @param strategy Strategy
     */
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
        parameterSets.clear();
        parameterSets.addAll(strategy.getStrategyParameterSets());
        selectedParameterSet = null;
        setTextFields();
    }

    /**
     * Sets the StrategyManagerController which is the controller for the grandparent.
     * @param strategyManagerController StrategyManagerController
     */
    public void setStrategyManagerController(StrategyManagerController strategyManagerController) {
        this.strategyManagerController = strategyManagerController;
    }

    /**
     * Sets the StrategyManagerService.
     * @param strategyManagerService StrategyManagerService
     */
    public void setStrategyManagerService(StrategyManagerService strategyManagerService) {
        this.strategyManagerService = strategyManagerService;
    }

    /**
     * Opens a ParameterSetDetails tab for the provided StrategyParameterSet.
     * @param parameterSet StrategyParameterSet
     */
    private void openParameterSetDetailsTab(StrategyParameterSet parameterSet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(PARAMETER_SET_DETAILS_FXML));
            Parent content = loader.load();
            ParameterSetDetailsController parameterSetDetailsController = loader.getController();
            parameterSetDetailsController.setParameterSet(parameterSet);
            parameterSetDetailsController.setStrategyManagerController(strategyManagerController);

            Tab tab = new Tab();
            tab.setText("ParameterSet: " + parameterSet.getName());
            tab.setContent(content);

            strategyManagerController.addTabToTabPane(tab);

        } catch(IOException e) {
            LOGGER.error("Failed to load ParameterSetDetails.fxml", e);
        }
    }

    /**
     * Sets all the dynamic texts fields to the appropriate values.
     */
    private void setTextFields() {
        if(strategy == null) {
            descriptionText.setText(MessageFormat.format(DESCRIPTION, ""));
            parentNameText.setText(MessageFormat.format(PARENT_NAME, ""));
        } else {
            descriptionText.setText(MessageFormat.format(DESCRIPTION, strategy.getDescription()));
            if(strategy.getParentStrategy() != null) {
                parentNameText.setText(MessageFormat.format(PARENT_NAME, strategy.getParentStrategy().toString()));
            }
        }
    }

}
