package com.github.tylerspaeth.strategy;

import com.github.tylerspaeth.common.ClasspathScanner;
import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.strategy.annotation.Strategy;
import com.github.tylerspaeth.strategy.annotation.StrategyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StrategyRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyRegistry.class);

    private final StrategyDAO strategyDAO;

    public StrategyRegistry(StrategyDAO strategyDAO) {
        this.strategyDAO = strategyDAO;
    }

    /**
     * Validates and populated the database with all Strategy classes on the classpath. RuntimeExceptions will be thrown
     * if validation fails.
     */
    public void initialize() {

        deactivateAllStrategies();

        List<Class<?>> strategyClasses = ClasspathScanner.getClassesWithAnnotation(Strategy.class);
        for (Class<?> strategyClass : strategyClasses) {

            if(!AbstractStrategy.class.isAssignableFrom(strategyClass)) {
                LOGGER.error("Failed to initialize strategy registry. Class found with Strategy annotation that does not extend AbstractStrategy. {}", strategyClass);
                throw new RuntimeException("Failed to initialize strategy registry. Class found with Strategy annotation that does not extend AbstractStrategy. " + strategyClass);
            }

            try {
                Method setStrategyEntityID = getSetStrategyEntityIDMethod(strategyClass);

                Strategy annotation = strategyClass.getAnnotation(Strategy.class);

                String strategyName = annotation.name().isEmpty() ? strategyClass.getName() : annotation.name();
                Integer strategyVersion = annotation.version();

                List<com.github.tylerspaeth.common.data.entity.Strategy> possibleStrategies = strategyDAO.getStrategiesByName(strategyName);

                if (possibleStrategies.isEmpty()) {
                    // New Strategy
                    if (strategyVersion != 0) {
                        LOGGER.error("Unable to initialize strategy: {}. First version of strategy must be version 0.", strategyName);
                        throw new RuntimeException("Unable to initialize strategy: " + strategyName + ". First version of strategy must be version 0.");
                    }
                    com.github.tylerspaeth.common.data.entity.Strategy strategy = new com.github.tylerspaeth.common.data.entity.Strategy();
                    strategy.setName(strategyName);
                    strategy.setVersion(strategyVersion);
                    strategy.setCreatedAt(Timestamp.from(Instant.now()));
                    strategy.setActive(true);
                    strategy.setLastUpdated(Timestamp.from(Instant.now()));

                    StrategyParameterSet parameterSet = new StrategyParameterSet();
                    strategy.getStrategyParameterSets().add(parameterSet);
                    parameterSet.setStrategy(strategy);
                    parameterSet.setName("Default");

                    buildDefaultParameterSet(strategyClass, parameterSet);

                    strategy = strategyDAO.update(strategy);

                    setStrategyEntityID.invoke(null, strategy.getStrategyID(), strategyClass);
                } else if (strategyVersion > possibleStrategies.getLast().getVersion()) {
                    // New Version of Existing Strategy
                    if (strategyVersion != possibleStrategies.getLast().getVersion() + 1) {
                        LOGGER.error("Unable to initialize strategy: {}. Strategy version should be incremented by one. Current version: {}. Provided Version: {}.", strategyName, possibleStrategies.getLast().getVersion(), strategyVersion);
                        throw new RuntimeException("Unable to initialize strategy: " + strategyName + ". Strategy version should be incremented by one. Current version: " + possibleStrategies.getLast().getVersion() + ". Provided Version: " + strategyVersion + ".");
                    }

                    com.github.tylerspaeth.common.data.entity.Strategy strategy = new com.github.tylerspaeth.common.data.entity.Strategy();
                    strategy.setName(strategyName);
                    strategy.setVersion(strategyVersion);
                    strategy.setCreatedAt(Timestamp.from(Instant.now()));
                    strategy.setActive(true);
                    strategy.setLastUpdated(Timestamp.from(Instant.now()));
                    strategy.setParentStrategy(possibleStrategies.getLast());

                    StrategyParameterSet parameterSet = new StrategyParameterSet();
                    strategy.getStrategyParameterSets().add(parameterSet);
                    parameterSet.setStrategy(strategy);
                    parameterSet.setName("Default");

                    buildDefaultParameterSet(strategyClass, parameterSet);
                    strategy = strategyDAO.update(strategy);

                    setStrategyEntityID.invoke(null, strategy.getStrategyID(), strategyClass);
                } else if (strategyVersion >= possibleStrategies.getFirst().getVersion() && strategyVersion <= possibleStrategies.getLast().getVersion()) {
                    // Existing Version of Existing Strategy
                    com.github.tylerspaeth.common.data.entity.Strategy matchedStrategy = possibleStrategies.stream().filter(strategy -> strategy.getVersion().equals(strategyVersion)).findFirst().get();
                    StrategyParameterSet parameterSet = matchedStrategy.getStrategyParameterSets().getFirst();
                    Set<String> parameterNames = parameterSet.getStrategyParameters().stream().map(com.github.tylerspaeth.common.data.entity.StrategyParameter::getName).collect(Collectors.toSet());

                    List<Field> annotatedFields = Arrays.stream(strategyClass.getDeclaredFields()).filter(field -> field.getAnnotation(StrategyParameter.class) != null).toList();
                    for (Field field : annotatedFields) {
                        if (!parameterNames.removeIf(name -> name.equals(field.getName()))) {
                            throw new RuntimeException("StrategyParameter " + field.getName() + " exists in the database but is missing from strategy " + strategyName + " version " + strategyVersion);
                        }
                    }
                    if (!parameterNames.isEmpty()) {
                        throw new RuntimeException("StrategyParameter(s) " + parameterNames + " existing in the strategy " + strategyName + " version " + strategyVersion + " but are not in the database.");
                    }

                    // We mark all strategies as inactive before starting to process any of them. If we find a match we can reactivate it
                    // unless it is already active. If it is already active it means a different strategy already activated it.
                    if (matchedStrategy.isActive()) {
                        throw new RuntimeException("More than one strategy exists with the name " + strategyName + " and version " + strategyVersion);
                    } else {
                        matchedStrategy.setActive(true);
                        matchedStrategy.setLastUpdated(Timestamp.from(Instant.now()));
                        matchedStrategy = strategyDAO.update(matchedStrategy);
                        setStrategyEntityID.invoke(null,  matchedStrategy.getStrategyID(), strategyClass);
                    }
                } else {
                    throw new RuntimeException("Unable to initialize strategy: " + strategyName + " version " + strategyVersion + ".");
                }
            } catch (InvocationTargetException | IllegalArgumentException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set strategyEntityID", e);
            }
        }
    }

    /**
     * Builds a default StrategyParameterSet with values all set to null based on the annotations in the strategyClass.
     * @param strategyClass Class with a Strategy annotation and fields with StrategyParameter annotations.
     * @param parameterSet StrategyParameterSet that StrategyParameters will be added to.
     */
    private void buildDefaultParameterSet(Class<?> strategyClass, StrategyParameterSet parameterSet) {
        List<Field> annotatedFields = Arrays.stream(strategyClass.getDeclaredFields()).filter(field -> field.getAnnotation(StrategyParameter.class) != null).toList();

        for(Field field : annotatedFields) {
            com.github.tylerspaeth.common.data.entity.StrategyParameter parameter = new com.github.tylerspaeth.common.data.entity.StrategyParameter();
            parameter.setStrategyParameterSet(parameterSet);
            parameterSet.getStrategyParameters().add(parameter);
            StrategyParameter annotation = field.getAnnotation(StrategyParameter.class);
            parameter.setName(annotation.name().isEmpty() ? field.getName() : annotation.name());
        }
    }

    /**
     * Sets all the strategies as inactive.
     */
    private void deactivateAllStrategies() {
        // TODO update this to use batched updates before this gets too slow
        List<com.github.tylerspaeth.common.data.entity.Strategy> strategies = strategyDAO.getAllStrategies();
        for(com.github.tylerspaeth.common.data.entity.Strategy strategy : strategies) {
            strategy.setActive(false);
            strategy.setLastUpdated(Timestamp.from(Instant.now()));
            strategyDAO.update(strategy);
        }
    }

    /**
     * Get the static setStrategyEntityID method from the strategy class.
     * @param strategyClass Class that extends AbstractStrategy
     * @return setStrategyEntityID method.
     * @throws NoSuchMethodException If the method does not exist on the class.
     */
    private Method getSetStrategyEntityIDMethod(Class<?> strategyClass) throws NoSuchMethodException {
        return strategyClass.getMethod("setStrategyEntityID", Integer.class, Class.class);
    }

}