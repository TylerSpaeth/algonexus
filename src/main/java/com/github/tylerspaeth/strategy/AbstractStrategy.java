package com.github.tylerspaeth.strategy;

import com.github.tylerspaeth.common.data.dao.BacktestResultDAO;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;
import com.github.tylerspaeth.strategy.annotation.StrategyParameterLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class that all strategies must inherit from.
 */
public abstract class AbstractStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStrategy.class);

    /**
     * This maps concrete AbstractStrategy implementations to the ID of the entity in the database.
     */
    private static final Map<Integer, Class<? extends AbstractStrategy>> STRATEGY_ENTITY_ID_MAP = new ConcurrentHashMap<>();

    private final OrderDAO orderDAO;
    private final BacktestResultDAO backtestResultDAO;

    private EngineCoordinator engineCoordinator;

    protected final StrategyParameterSet strategyParameterSet;
    private final AtomicReference<BacktestResult> backtestResult;

    // Currently this can only be set true once. If a strategy should be run again then a new instance should be created.
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread runningThread;

    protected final User user;

    /**
     * The child constructor must require the exact same arguments.
     */
    public AbstractStrategy(StrategyParameterSet strategyParameterSet, User user) {
        this.strategyParameterSet = strategyParameterSet;
        this.backtestResult = null;
        this.orderDAO = new OrderDAO();
        this.backtestResultDAO = new BacktestResultDAO();
        this.user = user;
    }

    /**
     * The child constructor must require the exact same arguments.
     */
    public AbstractStrategy(StrategyParameterSet strategyParameterSet, User user, BacktestResult backtestResult) {
        this.strategyParameterSet = strategyParameterSet;
        this.backtestResult = new AtomicReference<>(backtestResult);
        this.orderDAO = new OrderDAO();
        this.backtestResultDAO = new BacktestResultDAO();
        this.user = user;
    }

    /**
     * Runs the strategy.
     */
    public final void run() {

        if(engineCoordinator == null) {
            LOGGER.error("Unable to run a strategy without an EngineCoordinator.");
            return;
        }

        if(!running.compareAndSet(false, true)) {
            LOGGER.error("Unable to run a strategy that is already running.");
            return;
        }

        StrategyParameterLoader.populateParameters(this, strategyParameterSet);

        Thread thread = new Thread(() -> {
            try {
                if (backtestResult != null) {
                    backtestResult.updateAndGet(result -> {
                        result.setStartTime(Timestamp.from(Instant.now()));
                        return backtestResultDAO.update(result);
                    });
                }

                onRun();
            } finally {
                if(backtestResult != null) {
                    backtestResult.updateAndGet(result -> {
                        result.setEndTime(Timestamp.from(Instant.now()));
                        return backtestResultDAO.update(result);
                    });
                }
                LOGGER.info("{} finished running with {} parameter set.", strategyParameterSet.getStrategy(), strategyParameterSet);
            }
        }, strategyParameterSet.toString() + "-Thread");
        thread.start();
        runningThread = thread;

        LOGGER.info("{} started running with {} parameter set.", strategyParameterSet.getStrategy(), strategyParameterSet);
    }

    /**
     * Defines the run lifecycle of a concrete strategy implementation. Somewhere in this method it should check if
     * onStop has already been called and if so exit gracefully.
     */
    protected abstract void onRun();

    /**
     * Stops the strategy from continuing to run.
     */
    public final void stop() {

        if(!running.get())   {
            LOGGER.warn("Strategy already stopped. {}", strategyParameterSet.getStrategy());
            return;
        }

        LOGGER.info("Stop has been triggered for strategy: {} and parameter set: {}", strategyParameterSet.getStrategy(), strategyParameterSet);
        onStop();
        LOGGER.info("Stop has completed for strategy: {} and parameter set: {}", strategyParameterSet.getStrategy(), strategyParameterSet);
    }

    /**
     * The stop behavior that should be implemented by the concrete strategy class. After this is called, the onRun method
     * should return if it has not already.
     */
    protected abstract void onStop();

    /**
     * Submits an engine request and blocks until it returns. This should only be called for a concrete strategy class
     * to avoid blocking other threads.
     * @param engineRequest AbstractEngineRequest
     * @return Result of the engine request.
     * @param <T> Type to be returned by the engine.
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     * @throws IllegalStateException If the EngineCoordinator is null.
     */
    protected final <T> T submitEngineRequest(AbstractEngineRequest<T> engineRequest) throws ExecutionException, InterruptedException, IllegalStateException {
        if(engineCoordinator == null) {
            throw new IllegalStateException("Failed to submit request to engine and the EngineCoordinator is null.");
        }
        T result = engineCoordinator.submitRequest(engineRequest);
        if(result instanceof Order resultAsOrder && backtestResult != null) {
            resultAsOrder.setBacktestResult(backtestResult.get());
            orderDAO.update(resultAsOrder);
        }
        return result;
    }

    /**
     * Set the EngineCoordinate for engine requests to be fed to. This can only be called once and must not be null.
     * @param engineCoordinator EngineCoordinator that request will be submitted to.
     */
    public final void setEngineCoordinator(EngineCoordinator engineCoordinator) {
        if(engineCoordinator != null && this.engineCoordinator == null) {
            this.engineCoordinator = engineCoordinator;
        } else {
            LOGGER.error("Failed to set EngineCoordinator");
        }
    }

    /**
     * Set the strategyEntityID of a given concrete AbstractStrategy implementation. This will likely only be called through
     * reflection.
     * @param strategyClass Concrete class that extends AbstractStrategy.
     * @param strategyEntityID ID of the corresponding strategy entity in the database.
     */
    public static void setStrategyEntityID(Integer strategyEntityID, Class<? extends AbstractStrategy> strategyClass) {
        STRATEGY_ENTITY_ID_MAP.put(strategyEntityID, strategyClass);
    }

    /**
     * Get the constructor for an implementation of AbstractStrategy.
     * @param strategyEntityID The ID of the strategy in the database.
     * @param useBacktestResults Whether this constructor will be for a strategy to run a backtest.
     * @return Constructor<? extends AbstractStrategy>
     * @throws NoSuchMethodException NoSuchMethodException
     */
    public static Constructor<? extends AbstractStrategy> getConstructorForClass(Integer strategyEntityID, boolean useBacktestResults) throws NoSuchMethodException {
        Class<? extends AbstractStrategy> strategyClass = STRATEGY_ENTITY_ID_MAP.get(strategyEntityID);
        if(strategyClass == null) {
            throw new RuntimeException("Failed to getConstructorForClass. " + strategyEntityID);
        }
        if(useBacktestResults) {
            return strategyClass.getConstructor(StrategyParameterSet.class, User.class, BacktestResult.class);
        } else {
            return strategyClass.getConstructor(StrategyParameterSet.class, User.class);
        }
    }

}
