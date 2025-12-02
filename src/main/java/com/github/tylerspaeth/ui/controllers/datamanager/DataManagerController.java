package com.github.tylerspaeth.ui.controllers.datamanager;

import com.github.tylerspaeth.common.data.dao.HistoricalDatasetDAO;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.datamanager.DataManagerService;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;

public class DataManagerController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerController.class);

    private DataManagerService dataManagerService;

    @FXML
    private ListView<HistoricalDataset> datasetListView;
    private ObservableList<HistoricalDataset> datasets = FXCollections.observableArrayList();
    private HistoricalDataset selectedDataset;

    private static final String CREATE_MODAL_TITLE = "Create New Dataset";
    private static final int CREATE_MODAL_WIDTH = 800;
    private static final int CREATE_MODAL_HEIGHT = 800;

    private static final String EXPORT_FILE_CHOOSER_TITLE = "Choose Export Location";

    @FXML
    private Text datasetNameText;
    @FXML
    private Text datasetSourceText;
    @FXML
    private Text tickerText;
    @FXML
    private Text assetTypeText;
    @FXML
    private Text exchangeText;
    @FXML
    private Text timeIntervalText;
    @FXML
    private Text intervalUnitText;
    @FXML
    private Text datasetStartText;
    @FXML
    private Text datasetEndText;
    @FXML
    private Text lastUpdatedText;
    @FXML
    private Text rowCountText;

    private static final String DATASET_NAME = "Dataset Name: {0}";
    private static final String DATASET_SOURCE = "Dataset Source: {0}";
    private static final String DATASET_TICKER = "Ticker: {0}";
    private static final String DATASET_ASSET_TYPE = "Asset Type: {0}";
    private static final String DATASET_EXCHANGE = "Exchange: {0}";
    private static final String DATASET_TIME_INTERVAL = "Time Interval: {0}";
    private static final String DATASET_INTERVAL_UNIT = "Interval Unit: {0}";
    private static final String DATASET_START = "Dataset Start: {0}";
    private static final String DATASET_END = "Dataset End: {0}";
    private static final String LAST_UPDATED = "Last Updated: {0}";
    private static final String ROW_COUNT = "Row Count: {0}";

    private HistoricalDatasetDAO historicalDatasetDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historicalDatasetDAO = new HistoricalDatasetDAO();
        dataManagerService = new DataManagerService();

        datasets.addAll(historicalDatasetDAO.getAllHistoricalDatasets());
        datasetListView.setItems(datasets);
        datasetListView.getSelectionModel().selectedItemProperty().addListener((_obs, oldVal, newVal) -> {
            selectedDataset = newVal;
            setDatasetTextFields();
        });

        setDatasetTextFields();
    }

    /**
     * Sets the parameters in all of the dataset text fields
     */
    private void setDatasetTextFields() {
        if(selectedDataset == null) {
            datasetNameText.setText(MessageFormat.format(DATASET_NAME, ""));
            datasetSourceText.setText(MessageFormat.format(DATASET_SOURCE, ""));
            tickerText.setText(MessageFormat.format(DATASET_TICKER, ""));
            assetTypeText.setText(MessageFormat.format(DATASET_ASSET_TYPE, ""));
            exchangeText.setText(MessageFormat.format(DATASET_EXCHANGE, ""));
            timeIntervalText.setText(MessageFormat.format(DATASET_TIME_INTERVAL, ""));
            intervalUnitText.setText(MessageFormat.format(DATASET_INTERVAL_UNIT, ""));
            datasetStartText.setText(MessageFormat.format(DATASET_START, ""));
            datasetEndText.setText(MessageFormat.format(DATASET_END, ""));
            lastUpdatedText.setText(MessageFormat.format(LAST_UPDATED, ""));
            rowCountText.setText(MessageFormat.format(ROW_COUNT, ""));
        } else {
            datasetNameText.setText(MessageFormat.format(DATASET_NAME, selectedDataset.getDatasetName()));
            datasetSourceText.setText(MessageFormat.format(DATASET_SOURCE, selectedDataset.getDatasetSource()));
            tickerText.setText(MessageFormat.format(DATASET_TICKER, selectedDataset.getSymbol().getTicker()));
            assetTypeText.setText(MessageFormat.format(DATASET_ASSET_TYPE, selectedDataset.getSymbol().getAssetType()));
            exchangeText.setText(MessageFormat.format(DATASET_EXCHANGE, selectedDataset.getSymbol().getExchange().getName()));
            timeIntervalText.setText(MessageFormat.format(DATASET_TIME_INTERVAL, selectedDataset.getTimeInterval()));
            intervalUnitText.setText(MessageFormat.format(DATASET_INTERVAL_UNIT, selectedDataset.getIntervalUnit().name));
            datasetStartText.setText(MessageFormat.format(DATASET_START, selectedDataset.getDatasetStart()));
            datasetEndText.setText(MessageFormat.format(DATASET_END, selectedDataset.getDatasetEnd()));
            lastUpdatedText.setText(MessageFormat.format(LAST_UPDATED, selectedDataset.getLastUpdated()));
            rowCountText.setText(MessageFormat.format(ROW_COUNT, selectedDataset.getCandlesticks().size()));
        }

    }

    /**
     * Opens up the modal with the form for creating a new dataset
     */
    @FXML
    private void showCreateModal(ActionEvent actionEvent) {

        Stage primaryStage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(primaryStage);
        modalStage.setTitle(CREATE_MODAL_TITLE);

        try {
            Parent modal = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/github/tylerspaeth/fxml/datamanager/DataManagerCreateForm.fxml")));
            Scene modalScene = new Scene(modal, CREATE_MODAL_WIDTH, CREATE_MODAL_HEIGHT);
            modalStage.setScene(modalScene);
        } catch (IOException e) {
            LOGGER.error("Failed to load create modal.");
        }

        modalStage.showAndWait();
    }

    /**
     * Exports the dataset to a CSV file in the users location of choice
     */
    @FXML
    private void exportDataset(ActionEvent actionEvent) {
        if(selectedDataset == null) {
            LOGGER.warn("Unable to export dataset, no dataset selected.");
            return;
        }

        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(EXPORT_FILE_CHOOSER_TITLE);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File selectedFile = fileChooser.showSaveDialog(stage);

        if(selectedFile == null) {
            LOGGER.info("No file selected for export.");
            return;
        }

        dataManagerService.exportDatasetToCSV(selectedDataset, selectedFile);

    }
}
