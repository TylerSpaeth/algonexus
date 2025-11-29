package com.github.tylerspaeth.common.enums;

public enum MarketDataType {

    LIVE(1),
    FROZEN(2),
    DELAYED(3),
    FROZEN_DELAYED(4);

    public final int code;

    MarketDataType(int code) {
        this.code = code;
    }

}
