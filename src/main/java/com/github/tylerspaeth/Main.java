package com.github.tylerspaeth;

import com.github.tylerspaeth.config.DatasourceConfig;

public class Main {
    static void main() throws InterruptedException {

        // Initialization logic
        DatasourceConfig.validate();

        AppInitializer.initializeStrategyRegistry();

        Thread engineThread = AppInitializer.launchEngine();

        Thread uiThread = AppInitializer.launchUI();

        engineThread.join();
        uiThread.join();
    }
}