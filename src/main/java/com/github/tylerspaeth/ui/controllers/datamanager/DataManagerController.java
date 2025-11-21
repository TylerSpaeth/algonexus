package com.github.tylerspaeth.ui.controllers.datamanager;

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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class DataManagerController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerController.class);

    @FXML
    public ListView<String> datasetListView;
    private ObservableList<String> datasets = FXCollections.observableArrayList();
    private String selectedDataset;

    private static final String CREATE_MODAL_TITLE = "Create New Dataset";
    private static final int CREATE_MODAL_WIDTH = 800;
    private static final int CREATE_MODAL_HEIGHT = 800;

    private static final String EXPORT_FILE_CHOOSER_TITLE = "Choose Export Location";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // TODO load the datasets from the database

        datasetListView.setItems(datasets);
        datasetListView.getSelectionModel().selectedItemProperty().addListener((_obs, newVal, _oldVal) -> selectedDataset = newVal);
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
            LOGGER.info("No file selected, exiting export.");
        }

        // TODO export the dataset to csv
    }
}
