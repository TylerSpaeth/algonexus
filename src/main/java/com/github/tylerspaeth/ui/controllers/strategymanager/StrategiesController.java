package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.strategy.StrategyManagerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StrategiesController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategiesController.class);

    private static final String STRATEGY_DETAILS_FXML = "/com/github/tylerspaeth/fxml/strategymanager/StrategyDetails.fxml";

    private StrategyDAO strategyDAO;

    private StrategyManagerController strategyManagerController;

    private StrategyManagerService strategyManagerService;

    @FXML
    private ListView<Strategy> strategiesListView;
    private final ObservableList<Strategy> strategies = FXCollections.observableArrayList();
    private Strategy selectedStrategy;

    @Override
    public void initialize(URL _url, ResourceBundle _resourceBundle) {
        this.strategyDAO = new StrategyDAO();

        strategies.addAll(strategyDAO.getAllActiveStrategies());
        strategiesListView.setItems(strategies);
        strategiesListView.getSelectionModel().selectedItemProperty().addListener((_obs, _oldVal, newVal) -> {
            selectedStrategy = newVal;
        });
    }

    /**
     * Refreshes the ListView of Strategies by requerying the database.
     */
    @FXML
    private void refreshListView() {
        strategies.clear();
        strategies.addAll(strategyDAO.getAllActiveStrategies());
        if(!strategies.contains(selectedStrategy)) {
            selectedStrategy = null;
        }
    }

    /**
     * Try to open a new tab to view the details of the selected Strategy.
     */
    @FXML
    private void viewDetailsOnSelected() {

        if(selectedStrategy == null) {
            LOGGER.warn("Unable to show details as no Strategy is selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(STRATEGY_DETAILS_FXML));
            Parent content = loader.load();
            StrategyDetailsController strategyDetailsController = loader.getController();
            strategyDetailsController.setStrategy(selectedStrategy);
            strategyDetailsController.setStrategyManagerController(strategyManagerController);
            strategyDetailsController.setStrategyManagerService(strategyManagerService);

            Tab tab = new Tab();
            tab.setText("Strategy: " + selectedStrategy.toString());
            tab.setContent(content);

            strategyManagerController.addTabToTabPane(tab);

        } catch(IOException e) {
            LOGGER.error("Failed to load StrategyDetails.fxml", e);
        }

    }

    /**
     * Sets the StrategyManagerController which is the controller for the parent.
     * @param strategyManagerController StrategyManagerController
     */
    public void setStrategyManagerController(StrategyManagerController strategyManagerController) {
        this.strategyManagerController = strategyManagerController;
    }

    public void setStrategyManagerService(StrategyManagerService strategyManagerService) {
        this.strategyManagerService = strategyManagerService;
    }

}
