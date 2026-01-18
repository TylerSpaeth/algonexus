package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.HistoricalDatasetDAO;
import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.data.entity.Strategy;

import java.util.List;

/**
 * Controller for the Backtest UI elements.
 */
public class BacktestController {

    private final StrategyDAO strategyDAO;
    private final HistoricalDatasetDAO historicalDatasetDAO;

    public BacktestController() {
        strategyDAO = new StrategyDAO();
        historicalDatasetDAO = new HistoricalDatasetDAO();
    }

    /**
     * Retrieve all active strategies.
     * @return List of Strategy.
     */
    public List<Strategy> getAllActiveStrategies() {
        return strategyDAO.getAllActiveStrategies();
    }

    /**
     * Get all historical datasets.
     * @return List of HistoricalDatasets
     */
    public List<HistoricalDataset> getAllHistoricalDatasets() {
        return historicalDatasetDAO.getAllHistoricalDatasets();
    }
}
