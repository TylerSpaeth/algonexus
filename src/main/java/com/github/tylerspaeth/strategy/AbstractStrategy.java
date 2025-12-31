package com.github.tylerspaeth.strategy;

import com.github.tylerspaeth.common.data.entity.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that all strategies must inherit from.
 */
public abstract class AbstractStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStrategy.class);

    private Strategy strategyEntity;

    public void setStrategyEntity(Strategy strategyEntity) {
        if(this.strategyEntity != null) {
            this.strategyEntity = strategyEntity;
        } else {
            LOGGER.error("Strategy entity can not be updated.");
        }
    }

    public Strategy getStrategyEntity() {
        return strategyEntity;
    }

}
