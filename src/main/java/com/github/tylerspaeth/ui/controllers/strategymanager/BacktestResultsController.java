package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class BacktestResultsController implements Initializable {

    private static final String BACKTEST_RESULT_ID = "Backtest Result ID: {0}";
    private static final String BACKTEST_START_TIME = "Backtest Start Time: {0}";
    private static final String BACKTEST_END_TIME = "Backtest End Time: {0}";

    private StrategyParameterSet parameterSet;

    @FXML
    private ListView<BacktestResult> backtestResultsListView;
    private final ObservableList<BacktestResult> backtestResults = FXCollections.observableArrayList();
    private BacktestResult selectedBacktestResult;

    @FXML
    private Text backtestResultIDText;

    @FXML
    private Text backtestStartTimeText;

    @FXML
    private Text backtestEndTimeText;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(parameterSet != null) {
            backtestResults.addAll(parameterSet.getBacktestResults());
        }
        backtestResultsListView.setItems(backtestResults);
        backtestResultsListView.getSelectionModel().selectedItemProperty().addListener((_obs, _oldVal, newVal) -> {
            selectedBacktestResult = newVal;
            setTextFields();
        });

        setTextFields();
    }

    /**
     * Refreshes the ListView of BackTestResults by requerying the database.
     */
    @FXML
    private void refreshListView() {
        if(parameterSet != null) {
            setParameterSet(parameterSet);
        }
    }

    /**
     * Updates the dynamic text fields as appropriate.
     */
    private void setTextFields() {
        if(selectedBacktestResult != null) {
            backtestResultIDText.setText(MessageFormat.format(BACKTEST_RESULT_ID, selectedBacktestResult.getBacktestResultID()));
            backtestStartTimeText.setText(MessageFormat.format(BACKTEST_START_TIME, selectedBacktestResult.getStartTime()));
            backtestEndTimeText.setText(MessageFormat.format(BACKTEST_END_TIME, selectedBacktestResult.getEndTime()));
        } else {
            backtestResultIDText.setText(MessageFormat.format(BACKTEST_RESULT_ID, ""));
            backtestStartTimeText.setText(MessageFormat.format(BACKTEST_START_TIME, ""));
            backtestEndTimeText.setText(MessageFormat.format(BACKTEST_END_TIME, ""));
        }
    }

    /**
     * Sets the ParameterSet that BacktestResults should be shown for.
     * @param parameterSet StrategyParameterSet
     */
    public void setParameterSet(StrategyParameterSet parameterSet) {
        this.parameterSet = parameterSet;
        backtestResults.clear();
        backtestResults.addAll(parameterSet.getBacktestResults());
        selectedBacktestResult = null;
    }
}
