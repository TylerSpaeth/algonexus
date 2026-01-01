package com.github.tylerspaeth;

import com.github.tylerspaeth.broker.backtester.BacktesterDataFeedService;
import com.github.tylerspaeth.broker.backtester.BacktesterOrderService;
import com.github.tylerspaeth.broker.backtester.BacktesterSharedService;
import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.broker.ib.service.IBAccountService;
import com.github.tylerspaeth.broker.ib.service.IBDataFeedService;
import com.github.tylerspaeth.broker.ib.service.IBOrderService;
import com.github.tylerspaeth.common.data.dao.CandlestickDAO;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.strategy.StrategyRegistry;

import java.util.concurrent.Executors;

/**
 * Utility class for initialization/wiring logic that should be kept out of the main class.
 */
public class AppInitializer {

    /**
     * Initializes the StrategyRegistry
     */
    public static void initializeStrategyRegistry() {
        StrategyRegistry strategyRegistry = new StrategyRegistry(new StrategyDAO());
        strategyRegistry.initialize();
    }

    /**
     * Wires and launches the engine thread.
     * @return Thread that the engine is running on.
     */
    public static Thread launchEngine() {
        IBSyncWrapper.getInstance().connect(); // TODO Remove this
        OrderDAO orderDAO = new OrderDAO();
        SymbolDAO symbolDAO = new SymbolDAO();
        CandlestickDAO candlestickDAO = new CandlestickDAO();
        BacktesterSharedService backtesterSharedService = new BacktesterSharedService(orderDAO);
        EngineCoordinator engineCoordinator = new EngineCoordinator(Executors.newCachedThreadPool(),
                new IBAccountService(),
                new IBDataFeedService(),
                new IBOrderService(orderDAO),
                new BacktesterDataFeedService(backtesterSharedService, symbolDAO, candlestickDAO),
                new BacktesterOrderService(backtesterSharedService, orderDAO, symbolDAO));

        Thread engineThread = new Thread(engineCoordinator::run, "Engine-Thread");
        engineThread.start();
        return engineThread;
    }

    /**
     * Wires and launches the UI thread.
     * @return Thread that the UI is running on.
     */
    public static Thread launchUI() {
        Thread uiThread = new Thread(() -> {
            // TODO run the ui
        }, "UI-Thread");
        uiThread.start();
        return uiThread;
    }


}
