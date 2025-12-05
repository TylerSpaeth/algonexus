package com.github.tylerspaeth.common.enums;

/**
 * Enums for the various valid interval units.
 */
public enum IntervalUnitEnum {

    TICK("Tick", null),
    SECOND("Second", 1),
    MINUTE("Minute", 60),
    HOUR("Hour", 3600),
    DAY("Day", 21600),
    WEEK("Week", 1296000),
    MONTH("Month", 7776000),
    YEAR("Year", 46656000);

    public final String name;
    public final Integer secondsPer; // How many seconds in one unit of the interval

    IntervalUnitEnum(String name, Integer secondsPer) {
        this.name = name;
        this.secondsPer = secondsPer;
    }

    @Override
    public String toString() {
        return name;
    }



}
