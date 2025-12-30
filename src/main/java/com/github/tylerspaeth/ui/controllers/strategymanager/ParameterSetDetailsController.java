package com.github.tylerspaeth.ui.controllers.strategymanager;

import com.github.tylerspaeth.common.data.dao.StrategyParameterDAO;
import com.github.tylerspaeth.common.data.entity.StrategyParameter;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ParameterSetDetailsController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSetDetailsController.class);

    private StrategyParameterSet parameterSet;

    private StrategyParameterDAO strategyParameterDAO;

    @FXML
    private ListView<StrategyParameter> parameterListView;
    private ObservableList<StrategyParameter> strategyParameters = FXCollections.observableArrayList();
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

}
