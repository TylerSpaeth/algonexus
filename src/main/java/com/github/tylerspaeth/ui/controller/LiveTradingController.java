package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.engine.request.StrategyRunRequest;
import com.github.tylerspaeth.strategy.AbstractStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Controller for the live trading UI classes.
 */
public class LiveTradingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveTradingController.class);

    private final StrategyDAO strategyDAO;

    public LiveTradingController() {
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
     * Run a Strategy with the given StrategyParameterSet.
     * @param engineCoordinator EngineCoordinator that the strategy should run through.
     * @param user User that initiated the request.
     * @param strategyParameterSet StrategyParameterSet to run.
     */
    public void runStrategy(EngineCoordinator engineCoordinator, User user, StrategyParameterSet strategyParameterSet) {
        Integer strategyID = strategyParameterSet.getStrategy().getStrategyID();
        try {
            Constructor<? extends AbstractStrategy> strategyClassConstructor = AbstractStrategy.getConstructorForClass(strategyID, false);
            AbstractStrategy strategy = strategyClassConstructor.newInstance(strategyParameterSet, user);
            strategy.setEngineCoordinator(engineCoordinator);
            engineCoordinator.submitRequest(new StrategyRunRequest(strategy));
        } catch (Exception e) {
            LOGGER.error("Failed to run strategy.", e);
        }
    }

}
