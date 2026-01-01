package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.dao.StrategyParameterDAO;
import com.github.tylerspaeth.common.data.entity.StrategyParameter;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ParameterSetDetailsController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSetDetailsController.class);

    private static final String BACKTEST_RESULTS_FXML = "/com/github/tylerspaeth/fxml/strategymanager/BacktestResults.fxml";

    private StrategyParameterSet parameterSet;

    private StrategyParameterDAO strategyParameterDAO;

    private StrategyManagerController strategyManagerController;

    @FXML
    private ListView<StrategyParameter> parameterListView;
    private final ObservableList<StrategyParameter> strategyParameters = FXCollections.observableArrayList();
    private StrategyParameter selectedParameter;

    @FXML
    private Text nameText;
    @FXML
    private TextField valueTextField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        strategyParameterDAO = new StrategyParameterDAO();

        if(parameterSet != null) {
            strategyParameters.addAll(parameterSet.getStrategyParameters());
        }
        parameterListView.setItems(strategyParameters);
        parameterListView.getSelectionModel().selectedItemProperty().addListener((_obs, _oldVal, newVal) -> {
            selectedParameter = newVal;
            setTextFields();
        });
        valueTextField.focusedProperty().addListener((_obs, wasFocused, isNowFocused) -> {
            if(selectedParameter != null && !isNowFocused) {
                selectedParameter.setValue(valueTextField.getText());
                strategyParameterDAO.update(selectedParameter);
                parameterListView.refresh();
            }
        });
    }

    /**
     * Refreshes the ListView of StrategyParameterSets by requerying the database.
     */
    @FXML
    private void refreshListView() {
        if(parameterSet != null) {
            setParameterSet(parameterSet);
            nameText.setText("");
            valueTextField.setText("");
        }
    }

    /**
     * Open a new tab to view all the BacktestResults for this StrategyParameterSet.
     */
    @FXML
    private void viewBacktests() {

        if(parameterSet == null) {
            LOGGER.warn("Unable to show backtest results as no ParameterSet is assigned.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader((getClass().getResource(BACKTEST_RESULTS_FXML)));
            Parent content = loader.load();
            BacktestResultsController backtestResultsController = loader.getController();
            backtestResultsController.setParameterSet(parameterSet);

            Tab tab = new Tab();
            tab.setText("Backtests: " + parameterSet.getStrategy().toString() +  " - " + parameterSet.getName());
            tab.setContent(content);

            strategyManagerController.addTabToTabPane(tab);
        } catch(IOException e)  {
            LOGGER.error("Failed to load BacktestResults.fxml", e);
        }
    }

    /**
     * Sets the ParameterSet that details are to be shown for.
     * @param parameterSet StrategyParameterSet
     */
    public void setParameterSet(StrategyParameterSet parameterSet) {
        this.parameterSet = parameterSet;
        strategyParameters.clear();
        strategyParameters.addAll(parameterSet.getStrategyParameters());
        selectedParameter = null;
    }

    /**
     * Updates the dynamic text fields as appropriate.
     */
    private void setTextFields() {
        if(selectedParameter != null) {
            nameText.setText(selectedParameter.getName());
            valueTextField.setText(selectedParameter.getValue());
        }
    }

    public void setStrategyManagerController(StrategyManagerController strategyManagerController) {
        this.strategyManagerController = strategyManagerController;
    }

}
