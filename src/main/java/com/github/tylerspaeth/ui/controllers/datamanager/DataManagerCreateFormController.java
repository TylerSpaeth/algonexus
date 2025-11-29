package com.github.tylerspaeth.ui.controllers.datamanager;

import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.github.tylerspaeth.datamanager.DataManagerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class DataManagerCreateFormController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerCreateFormController.class);

    private static final String FILE_CHOOSER_TITLE = "Select File to Load Dataset From";

    private DataManagerService dataManagerService;

    @FXML
    private TextField datasetNameTextField;

    @FXML
    private TextField datasetSourceTextField;

    @FXML
    private ComboBox<Symbol> tickerComboBox;
    private final ObservableList<Symbol> tickerOptions = FXCollections.observableArrayList();

    @FXML
    private TextField timeIntervalTextField;

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

    @FXML
    private TextField dateFormatTextField;

    @Override
    public void initialize(URL _url, ResourceBundle _resourceBundle) {
        dataManagerService = new DataManagerService();
        initializeTickerComboBox();
        initializeIntervalUnitComboBox();

        timeIntervalTextField.setTextFormatter(integerTextFormatter());
        metadataRowCountTextField.setTextFormatter(integerTextFormatter());
    }

    /**
     * Loads the tickerComboBox with the available Ticker symbols.
     */
    private void initializeTickerComboBox() {
        if(tickerOptions.isEmpty()) {
            SymbolDAO symbolDAO = new SymbolDAO();
            tickerOptions.addAll(symbolDAO.getAllSymbols());
        }
        tickerComboBox.setItems(tickerOptions);
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
    private void submitForm(ActionEvent actionEvent) {

        if(!validateRequiredFields()) {
            LOGGER.warn("Unabled to submit form, required fields not filled.");
            return;
        }

        LOGGER.info("Submitting form for dataset: {}", datasetNameTextField.getText());

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName(datasetNameTextField.getText());
        historicalDataset.setDatasetSource(datasetSourceTextField.getText());
        historicalDataset.setSymbol(tickerComboBox.getValue());
        historicalDataset.setTimeInterval(Integer.parseInt(timeIntervalTextField.getText()));
        historicalDataset.setIntervalUnit(intervalUnitComboBox.getValue());

        // Attempt upload
        boolean uploadSuccess = dataManagerService.loadDatasetFromCSV(historicalDataset,
                selectedFile,
                fileFormatTextField.getText(),
                Integer.parseInt(metadataRowCountTextField.getText()),
                new SimpleDateFormat(dateFormatTextField.getText()));

        // Modal will close if upload is successful, otherwise it stays open
        if(uploadSuccess) {
            LOGGER.info("Upload successful for dataset: {}", datasetNameTextField.getText());
            ((Stage) ((Node)actionEvent.getSource()).getScene().getWindow()).close();
        }
        else {
            LOGGER.error("Failed to upload dataset: {}", datasetNameTextField.getText());
        }


    }

    /**
     * Checks if all the required fields have been filled.
     * @return True if all required fields are populated, false otherwise
     */
    private boolean validateRequiredFields() {

        return !datasetNameTextField.getText().isBlank() &&
                !tickerComboBox.getSelectionModel().isEmpty() &&
                !timeIntervalTextField.getText().isBlank() &&
                !intervalUnitComboBox.getSelectionModel().isEmpty() &&
                selectedFile != null &&
                !fileFormatTextField.getText().isBlank() &&
                !metadataRowCountTextField.getText().isBlank() &&
                !dateFormatTextField.getText().isBlank();
    }

    /**
     * Gets a text formatter that only allows integers
     */
    private TextFormatter<Integer> integerTextFormatter() {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if(newText.matches("-?([0-9]*)")) {
                return change;
            }
            return null;
        };

        return new TextFormatter<>(new IntegerStringConverter(), null, integerFilter);
    }

}
