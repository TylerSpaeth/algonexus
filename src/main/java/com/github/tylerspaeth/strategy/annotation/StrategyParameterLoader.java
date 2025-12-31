package com.github.tylerspaeth.strategy.annotation;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.strategy.AbstractStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Class for loading fields with StrategyParameter annotations. This only supports loading
 * Boolean, Byte, Short, Int, Long, Float, Double, String, and Enum values along with primitive equivalents.
 */
public class StrategyParameterLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyParameterLoader.class);

    /**
     * Populates all the fields marked with StrategyParameter annotations with values from the provided StrategyParameterSet.
     * @param strategy AbstractStrategy containing StrategyParameter annotations.
     * @param parameterSet StrategyParameterSet containing values that should populate all the annotations on this strategy.
     */
    public static void populateParameters(AbstractStrategy strategy, StrategyParameterSet parameterSet) {
        for (Field field : strategy.getClass().getDeclaredFields()) {
            StrategyParameter annotation = field.getAnnotation(StrategyParameter.class);
            if(annotation == null) {
                continue;
            }

            String paramName = annotation.name().isEmpty() ? field.getName() : annotation.name();
            Optional<com.github.tylerspaeth.common.data.entity.StrategyParameter> dbValue = parameterSet.getStrategyParameters().stream().filter(param -> param.getName().equals(paramName)).findFirst();

            if(dbValue.isEmpty()) {
                LOGGER.warn("Unable to find value for parameter with name: ({}) in set: ({}).", paramName, parameterSet.getName());
                continue;
            }

            field.setAccessible(true);
            try {
                field.set(strategy, castValue(field.getType(), dbValue.get().getValue()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Casts the provides String value to the appropriate data type.
     * @param type Datatype the value should be cast to.
     * @param value String representation that needs to be cast.
     * @return Object that has been cast to the correct type.
     */
    private static Object castValue(Class<?> type, String value) {
        if(List.of(byte.class, Byte.class).contains(type)) return Byte.parseByte(value);
        if(List.of(short.class, Short.class).contains(type)) return Short.parseShort(value);
        if(List.of(int.class, Integer.class).contains(type)) return Integer.parseInt(value);
        if(List.of(long.class, Long.class).contains(type)) return Long.parseLong(value);
        if(List.of(float.class, Float.class).contains(type)) return Float.parseFloat(value);
        if(List.of(double.class, Double.class).contains(type)) return Double.parseDouble(value);
        if(List.of(boolean.class, Boolean.class).contains(type)) return Boolean.parseBoolean(value);
        if(type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), value);
        return value; // For strings or unsupported types
    }
}
