package com.github.tylerspaeth.strategy;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.StrategyParameter;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;

public class StrategyManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyManagerService.class);

    private final StrategyDAO strategyDAO;

    public StrategyManagerService(StrategyDAO strategyDAO) {
        this.strategyDAO = strategyDAO;
    }

    /**
     * Adds a new StrategyParameterSet to an existing strategy copying the field names from an existing set.
     * @param strategy Strategy to add the set to.
     * @param name Name of the new set.
     * @param description Optional description to add to the set.
     * @return true if the add is successful, false otherwise
     */
    public boolean addNewParameterSetToExistingStrategy(Strategy strategy, String name, String description) {
        if(strategy.getStrategyParameterSets().isEmpty() || name == null || name.isBlank()) {
            LOGGER.error("Unable to create new parameter set for strategy: {}.", strategy);
            return false;
        }
        StrategyParameterSet copyFromParameterSet = strategy.getStrategyParameterSets().getFirst();
        StrategyParameterSet parameterSet = new StrategyParameterSet();
        parameterSet.setName(name);
        parameterSet.setDescription(description);
        parameterSet.setStrategy(strategy);
        strategy.getStrategyParameterSets().add(parameterSet);

        for(StrategyParameter strategyParameter : copyFromParameterSet.getStrategyParameters()) {
            StrategyParameter newParameter = new StrategyParameter();
            newParameter.setName(strategyParameter.getName());
            newParameter.setStrategyParameterSet(parameterSet);
            parameterSet.getStrategyParameters().add(newParameter);
        }

        strategy.setLastUpdated(Timestamp.from(Instant.now()));
        strategyDAO.update(strategy);

        return true;
    }

    /**
     * Helper function to get the updated Strategy if it may have been modified elsewhere.
     * @param strategy Strategy
     * @return Strategy
     */
    public Strategy refreshStrategy(Strategy strategy) {
        return strategyDAO.findByStrategyID(strategy.getStrategyID());
    }

}
