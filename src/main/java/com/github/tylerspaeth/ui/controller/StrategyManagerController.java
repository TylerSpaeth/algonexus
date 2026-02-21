package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.Strategy;

import java.util.List;

/**
 * Controller class for StrategyManagerMenu.
 */
public class StrategyManagerController {

    private final StrategyDAO strategyDAO;

    public StrategyManagerController() {
        strategyDAO = new StrategyDAO();
    }

    /**
     * Retrieve all active strategies.
     * @return List of Strategy.
     */
    public List<Strategy> getAllActiveStrategies() {
        return strategyDAO.getAllActiveStrategies();
    }

    /**
     * Get the updated version of the strategy.
     * @param strategy Possible outdated strategy
     * @return Up-to-date strategy
     */
    public Strategy getUpdatedStrategy(Strategy strategy) {
        return strategyDAO.findByStrategyID(strategy.getStrategyID());
    }

}
