package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.Strategy;

import java.util.List;

/**
 * Controller for the Backtest UI elements.
 */
public class BacktestController {

    private final StrategyDAO strategyDAO;

    public BacktestController() {
        strategyDAO = new StrategyDAO();
    }

    /**
     * Retrieve all active strategies.
     * @return List of Strategy.
     */
    public List<Strategy> getAllActiveStrategies() {
        return strategyDAO.getAllActiveStrategies();
    }
}
