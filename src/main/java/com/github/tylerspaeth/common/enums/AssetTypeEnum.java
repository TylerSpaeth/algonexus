package com.github.tylerspaeth.common.enums;

/**
 * Enums for the various Asset Type that are available.
 */
public enum AssetTypeEnum {

    EQUITIES("Equities"),
    FUTURES("Futures"),
    OPTIONS("Options"),
    FOREX("Forex"),
    CRYPTOCURRENCY("Cryptocurrency"),
    OTHER("Other");

    public final String name;

    AssetTypeEnum(String name) {
        this.name = name;
    }

}
