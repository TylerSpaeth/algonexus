package com.github.tylerspaeth.broker.response;

import java.sql.Timestamp;

public record OHLCV(Timestamp time, double open, double high, double low, double close, double volume) {
}
