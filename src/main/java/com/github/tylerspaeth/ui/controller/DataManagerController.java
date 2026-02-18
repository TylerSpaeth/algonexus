package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.HistoricalDatasetDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.github.tylerspaeth.datamanager.DataManagerService;
import jakarta.persistence.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Controller for the Data Manager UI classes.
 */
public class DataManagerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerController.class);

    private final SymbolDAO symbolDAO;
    private final HistoricalDatasetDAO historicalDatasetDAO;
    private final DataManagerService dataManagerService;

    public DataManagerController() {
        symbolDAO = new SymbolDAO();
        historicalDatasetDAO = new HistoricalDatasetDAO();
        dataManagerService = new DataManagerService(historicalDatasetDAO);
    }

    public List<HistoricalDataset> getAllHistoricalDatasets() {
        return historicalDatasetDAO.getAllHistoricalDatasets();
    }

    // TODO run this in a background thread. Currently it blocks the UI and takes 5+ minutes per upload
    /**
     * Attempt to create a dataset with the provided data
     * @param name Name of the new dataset
     * @param source Where this data came from.
     * @param tickerSymbol Ticker symbol the data is for.
     * @param timeInterval How many of the interval unit each candlestick is.
     * @param intervalUnitEnum IntervalUnitEnum (ex. SECOND, MINUTE)
     * @param sourceFileLocation Path to the file to load from.
     * @param sourceFileMetadataRows Number of metadata rows the file has.
     * @param sourceFileColumnOrder Order of the columns in the file
     * @param sourceFileDateFormat Format that dates are stored as in the file
     * @return true if the upload has started successfully, false otherwise
     */
    public boolean tryToCreateDataset(String name, String source, String tickerSymbol, int timeInterval,
                                                IntervalUnitEnum intervalUnitEnum, String sourceFileLocation,
                                                int sourceFileMetadataRows, String sourceFileColumnOrder,
                                                String sourceFileDateFormat) {

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName(name);
        historicalDataset.setDatasetSource(source);
        historicalDataset.setTimeInterval(timeInterval);
        historicalDataset.setIntervalUnit(intervalUnitEnum);

        Symbol symbol = null;
        try {
            symbol = symbolDAO.getSymbolByTicker(tickerSymbol);
        } catch (NonUniqueResultException e) {
            LOGGER.error("Multiple symbols found for ticker: {}", tickerSymbol);
        }
        if(symbol == null) {
            return false;
        }
        historicalDataset.setSymbol(symbol);

        return dataManagerService.loadDatasetFromCSV(historicalDataset, new File(sourceFileLocation), sourceFileColumnOrder,
                sourceFileMetadataRows, new SimpleDateFormat(sourceFileDateFormat));
    }
}
