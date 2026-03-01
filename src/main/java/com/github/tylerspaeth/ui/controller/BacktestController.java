package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.*;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.engine.request.StrategyRunRequest;
import com.github.tylerspaeth.statistics.StatisticsUtils;
import com.github.tylerspaeth.strategy.AbstractStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Controller for the Backtest UI elements.
 */
public class BacktestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktestController.class);

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

    /**
     * Calculates the PnL including fees of a collection of trades.
     * @param trades List<Trade>
     * @return PnL including fees
     */
    public float calculatePnL(List<Trade> trades) {
        return StatisticsUtils.calculatePnL(trades);
    }

    /**
     * Calculates the number of positions that were taken in a collection of trades. A position is a duration in which
     * a nonzero quantity is held and only closes once the quantity held hits 0 or a reversal happens (ex. Going from long on a position to short).
     * @param trades List<Trade>
     * @return Number of positions that were taken.
     */
    public int calculatePositionsTaken(List<Trade> trades) {
        return StatisticsUtils.calculatePositionsTaken(trades);
    }

    /**
     * Calculate the Sharpe Ratio for a BacktestResult based on a risk-free rate of 0%.
     * @param backtestResult Completed BacktestResult to calculate Sharpe Ratio on.
     * @return Sharpe Ratio
     */
    public float calculateSharpeRatio(BacktestResult backtestResult) {
        return StatisticsUtils.calculateSharpeRatio(backtestResult, 0);
    }

    /**
     * Run a backtest with the given StrategyParameterSet.
     * @param engineCoordinator EngineCoordinator that the backtest should run through.
     * @param user User that initiated the request.
     * @param strategyParameterSet StrategyParameterSet to run a backtest on.
     */
    public void runBacktest(EngineCoordinator engineCoordinator, User user, StrategyParameterSet strategyParameterSet, Float startingBalance) {
        Integer strategyID = strategyParameterSet.getStrategy().getStrategyID();
        try {
            Constructor<? extends AbstractStrategy> strategyClassConstructor = AbstractStrategy.getConstructorForClass(strategyID, true);
            BacktestResult backtestResult = new BacktestResult();
            backtestResult.setStartingBalance(startingBalance);
            backtestResult.setStrategyParameterSet(strategyParameterSet);
            AbstractStrategy strategy = strategyClassConstructor.newInstance(strategyParameterSet, user, backtestResult);
            strategy.setEngineCoordinator(engineCoordinator);
            engineCoordinator.submitRequest(new StrategyRunRequest(strategy));
        } catch (Exception e) {
            LOGGER.error("Failed to run backtest.", e);
        }
    }
}
