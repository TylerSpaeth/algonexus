package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.strategy.StrategyManagerService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class StrategyManagerController implements Initializable {

    @FXML
    private TabPane strategyManagerTabPane;

    @FXML
    private StrategiesController strategiesController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(strategiesController != null) {
            strategiesController.setStrategyManagerController(this);
            strategiesController.setStrategyManagerService(new StrategyManagerService(new StrategyDAO()));
        }
    }

    /**
     * Adds a new tab to the tab pane.
     * @param tab Tab
     */
    public void addTabToTabPane(Tab tab) {
        strategyManagerTabPane.getTabs().add(tab);
    }
}
