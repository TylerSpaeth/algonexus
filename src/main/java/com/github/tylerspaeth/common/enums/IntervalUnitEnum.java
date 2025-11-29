package com.github.tylerspaeth.common.enums;

/**
 * Enums for the various valid interval units.
 */
public enum IntervalUnitEnum {

    TICK("Tick"),
    SECOND("Second"),
    MINUTE("Minute"),
    HOUR("Hour"),
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year");

    public final String name;

    IntervalUnitEnum(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }



}
