package com.github.tylerspaeth.ui.controllers.datamanager;

import com.github.tylerspaeth.enums.IntervalUnitEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DataManagerCreateFormController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerCreateFormController.class);

    private static final String FILE_CHOOSER_TITLE = "Select File to Load Dataset From";

    @FXML
    private TextField datasetNameTextField;

    @FXML
    private TextField datasetSourceTextField;

    @FXML
    private ComboBox tickerComboBox;

    @FXML
    private TextField intervalTextField;

    @FXML
    private ComboBox<IntervalUnitEnum> intervalUnitComboBox;
    private final ObservableList<IntervalUnitEnum> intervalOptions = FXCollections.observableArrayList();

    @FXML
    private Text fileNameText;
    private File selectedFile;

    @FXML
    private TextField fileFormatTextField;

    @FXML
    private TextField metadataRowCountTextField;

    @Override
    public void initialize(URL _url, ResourceBundle _resourceBundle) {
        initializeTickerComboBox();
        initializeIntervalUnitComboBox();
    }

    /**
     * Loads the tickerComboBox with the available Ticker symbols.
     */
    private void initializeTickerComboBox() {
        // TODO load ComboBox options
    }

    /**
     * Loads the intervalUnitComboBox with the available interval units.
     */
    private void initializeIntervalUnitComboBox() {
        if(intervalOptions.isEmpty()) {
            intervalOptions.addAll(IntervalUnitEnum.values());
        }
        intervalUnitComboBox.setItems(intervalOptions);
    }

    /**
     * Opens a FileChooser that allows the user to select a csv file. The file is then set as the selected file and its
     * name is displayed.
     */
    @FXML
    private void selectDataFile(ActionEvent actionEvent) {

        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(FILE_CHOOSER_TITLE);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null) {
            fileNameText.setText(selectedFile.getName());
        }
        else {
            fileNameText.setText("");
        }
    }

    /**
     * Handles submitting the form. Validates that the required fields are filled and if they are, it submits.
     * Submission of the form will close the modal and cause the data to be entered into the database.
     */
    @FXML
    private void submitForm() {

        if(!validateRequiredFields()) {
            LOGGER.warn("Unabled to submit form, required fields not filled.");
            return;
        }

        LOGGER.info("Submitting form for dataset: {}", datasetNameTextField.getText());

        // TODO handle actual form submission

    }

    /**
     * Checks if all the required fields have been filled.
     * @return True if all required fields are populated, false otherwise
     */
    private boolean validateRequiredFields() {

        return !datasetNameTextField.getText().isBlank() &&
                !tickerComboBox.getSelectionModel().isEmpty() &&
                !intervalTextField.getText().isBlank() &&
                !intervalUnitComboBox.getSelectionModel().isEmpty() &&
                selectedFile != null &&
                !fileFormatTextField.getText().isBlank() &&
                !metadataRowCountTextField.getText().isBlank();
    }

}
