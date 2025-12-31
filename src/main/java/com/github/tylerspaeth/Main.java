package com.github.tylerspaeth;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.strategy.StrategyRegistry;
import com.github.tylerspaeth.ui.GUI;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        StrategyRegistry strategyRegistry = new StrategyRegistry(new StrategyDAO());
        strategyRegistry.initialize();

        DatasourceConfig.validate();

        Application.launch(GUI.class, "");

    }
}