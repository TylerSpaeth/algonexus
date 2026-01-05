package com.github.tylerspaeth;

import com.github.tylerspaeth.broker.backtester.BacktesterDataFeedService;
import com.github.tylerspaeth.broker.backtester.BacktesterOrderService;
import com.github.tylerspaeth.broker.backtester.BacktesterSharedService;
import com.github.tylerspaeth.broker.ib.service.IBAccountService;
import com.github.tylerspaeth.broker.ib.service.IBDataFeedService;
import com.github.tylerspaeth.broker.ib.service.IBOrderService;
import com.github.tylerspaeth.common.data.dao.CandlestickDAO;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.strategy.StrategyRegistry;
import com.github.tylerspaeth.ui.TUI;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.SignInMenu;

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
     * Create the EngineCoordinator that will be used the engine and ui threads
     * @return EngineCoordinator
     */
    public static EngineCoordinator createEngine() {
        OrderDAO orderDAO = new OrderDAO();
        SymbolDAO symbolDAO = new SymbolDAO();
        CandlestickDAO candlestickDAO = new CandlestickDAO();
        BacktesterSharedService backtesterSharedService = new BacktesterSharedService(orderDAO);
        return new EngineCoordinator(Executors.newCachedThreadPool(),
                new IBAccountService(),
                new IBDataFeedService(),
                new IBOrderService(orderDAO),
                new BacktesterDataFeedService(backtesterSharedService, symbolDAO, candlestickDAO),
                new BacktesterOrderService(backtesterSharedService, orderDAO, symbolDAO));
    }

    /**
     * Wires and launches the engine thread.
     * @param engineCoordinator EngineCoordinator
     * @return Thread that the engine is running on.
     */
    public static Thread launchEngine(EngineCoordinator engineCoordinator) {
        Thread engineThread = new Thread(engineCoordinator::run, "Engine-Thread");
        engineThread.start();
        return engineThread;
    }

    /**
     * Wires and launches the UI thread.
     * @param engineCoordinator EngineCoordinator
     * @return Thread that the UI is running on.
     */
    public static Thread launchUI(EngineCoordinator engineCoordinator) {
        Thread uiThread = new Thread(() -> {
                new TUI(new UIContext(engineCoordinator)).run(new SignInMenu());
        }, "UI-Thread");
        uiThread.start();
        return uiThread;
    }


}
