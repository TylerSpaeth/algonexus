package com.github.tylerspaeth;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.strategy.StrategyRegistry;

public class Main {
    public static void main(String[] args) {

        DatasourceConfig.validate();

        StrategyRegistry strategyRegistry = new StrategyRegistry(new StrategyDAO());
        strategyRegistry.initialize();

    }
}