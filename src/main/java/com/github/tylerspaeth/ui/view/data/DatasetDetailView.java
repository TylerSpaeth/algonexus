package com.github.tylerspaeth.ui.view.data;

import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.ui.view.common.AbstractDetailView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * See a detailed view of a dataset.
 */
public class DatasetDetailView extends AbstractDetailView {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetDetailView.class);

    private static final String DETAIL_VIEW_TEXT =
            """
            Dataset Name: {0}
            Dataset Source: {1}
            Ticker Symbol: {2}
            Interval: {3} {4}
            Start Time: {5}
            End Time: {6}
            Number of Candlesticks: {7}
            """;

    public DatasetDetailView(AbstractView parent) {
        super(parent);
    }

    /**
     * Set the HistoricalDataset, updating the displayed text.
     * @param historicalDataset HistoricalDataset
     */
    public void setHistoricalDataset(HistoricalDataset historicalDataset) {
        if(historicalDataset == null) {
            LOGGER.error("Unable to display null dataset.");
            return;
        }
        setText(MessageFormat.format(DETAIL_VIEW_TEXT,
                                     historicalDataset.getDatasetName(),
                                     historicalDataset.getDatasetSource(),
                                     historicalDataset.getSymbol().getTicker(),
                                     historicalDataset.getTimeInterval(),
                                     historicalDataset.getIntervalUnit().toString(),
                                     historicalDataset.getDatasetStart(),
                                     historicalDataset.getDatasetEnd(),
                                     historicalDataset.getCandlesticks().size()));
    }

}
