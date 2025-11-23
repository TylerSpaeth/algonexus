package com.github.tylerspaeth.service;

import com.github.tylerspaeth.data.dao.HistoricalDatasetDAO;
import com.github.tylerspaeth.data.entity.Candlestick;
import com.github.tylerspaeth.data.entity.HistoricalDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;

public class DataManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerService.class);

    private final HistoricalDatasetDAO historicalDatasetDAO;

    private static final String CSV_DELIMITER = ",";
    private static final int MIN_COLUMN_COUNT = 6;

    public DataManagerService() {
        this.historicalDatasetDAO = new HistoricalDatasetDAO();
    }

    /**
     * Loads a HistoricalDataset with data from a CSV file. Updates the timestamps and candlesticks accordingly.
     * Persists the changes.
     * @param historicalDataset The historical dataset with will have data loaded into it.
     * @param file The csv file with candlestick data
     * @param format Column format of the csv file with characters representing each column
     * @param metadataRows The number of metadata rows at the top of the file to skip
     * @param dateFormat The format that the date column of the file is in
     * @return true if loading succeeds, false otherwise
     */
    public boolean loadDatasetFromCSV(HistoricalDataset historicalDataset, File file, String format, int metadataRows, SimpleDateFormat dateFormat) {

        // Find the locations for all the columns
        int dateCol = format.indexOf("D");
        int openCol = format.indexOf("O");
        int closeCol = format.indexOf("C");
        int highCol = format.indexOf("H");
        int lowCol = format.indexOf("L");
        int volumeCol = format.indexOf("V");

        if(dateCol == -1 || openCol == -1 || closeCol == -1 || highCol == -1 || lowCol == -1 || volumeCol == -1) {
            LOGGER.error("Missing definition for metadata column(s).");
            return false;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {

            // Skip metadata
            for(int i = 0; i < metadataRows; i++) {
                reader.readLine();
            }

            Timestamp startTime = new Timestamp(Long.MAX_VALUE);
            Timestamp endTime = new Timestamp(Long.MIN_VALUE);

            String line;
            while((line = reader.readLine()) != null) {

                String[] splitLine = line.split(CSV_DELIMITER);
                if(splitLine.length < MIN_COLUMN_COUNT || splitLine.length != format.length()) {
                    LOGGER.error("Row does not meet expected criteria: {}", line);
                    return false;
                }

                // Calculate the timestamp and if it is the start or end of the data
                var timestamp = Timestamp.from(dateFormat.parse(splitLine[dateCol]).toInstant());
                if(timestamp.before(startTime)) {
                    startTime = timestamp;
                }
                if(timestamp.after(endTime)) {
                    endTime = timestamp;
                }

                Candlestick candlestick = new Candlestick();
                candlestick.setTimestamp(timestamp);
                candlestick.setOpen(Float.parseFloat(splitLine[openCol]));
                candlestick.setClose(Float.parseFloat(splitLine[closeCol]));
                candlestick.setHigh(Float.parseFloat(splitLine[highCol]));
                candlestick.setLow(Float.parseFloat(splitLine[lowCol]));
                candlestick.setVolume(Float.parseFloat(splitLine[volumeCol]));
                candlestick.setHistoricalDataset(historicalDataset);
                if(historicalDataset.getCandlesticks() == null) {
                    historicalDataset.setCandlesticks(new ArrayList<>());
                }
                historicalDataset.getCandlesticks().add(candlestick);

            }

            historicalDataset.setDatasetStart(startTime);
            historicalDataset.setDatasetEnd(endTime);
            historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));

            historicalDatasetDAO.save(historicalDataset);

        } catch (Exception e) {
            LOGGER.error("Loading from file failed: {}", file.getName());
            return false;
        }

        return true;

    }

}
