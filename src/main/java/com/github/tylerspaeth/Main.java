package com.github.tylerspaeth;

import com.github.tylerspaeth.config.DatasourceConfig;
import com.github.tylerspaeth.engine.EngineCoordinator;

public class Main {
    static void main() throws InterruptedException {

        // Initialization logic
        DatasourceConfig.validate();

        AppInitializer.initializeStrategyRegistry();

        EngineCoordinator engineCoordinator = AppInitializer.createEngine();

        Thread engineThread = AppInitializer.launchEngine(engineCoordinator);

        Thread uiThread = AppInitializer.launchUI(engineCoordinator);

        engineThread.join();
        uiThread.join();
    }
}