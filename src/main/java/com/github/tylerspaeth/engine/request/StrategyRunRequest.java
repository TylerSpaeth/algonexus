package com.github.tylerspaeth.engine.request;

import com.github.tylerspaeth.strategy.AbstractStrategy;

import java.util.HashSet;
import java.util.Set;

/**
 * Request to run a strategy. This request should be limited by checking if it can be run before executing it.
 */
public class StrategyRunRequest extends AbstractEngineRequest<Void> {

    private static final int MAX_CONCURRENT_STRATEGIES = 4;

    private static final Set<AbstractStrategy> runningStrategies = new HashSet<>();

    private final AbstractStrategy strategy;

    public StrategyRunRequest(AbstractStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    protected Void execute() {
        runningStrategies.add(strategy);
        strategy.run();
        return null;
    }

    /**
     * Checks if a strategy can be run. For it to be safe to run the number of running strategies must be less than the
     * max acceptable and the strategy can not be running already.
     * @return true if the strategy can be run, false otherwise
     */
    public boolean canStrategyBeRun() {

        runningStrategies.removeIf(runningStrategy -> !runningStrategy.isRunning());

        if(runningStrategies.size() >= MAX_CONCURRENT_STRATEGIES) {
            return false;
        }
        return !runningStrategies.contains(strategy);
    }
}
