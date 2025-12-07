package com.github.tylerspaeth.broker.response;

import com.ib.client.Decimal;

public record RealtimeBar(long date, double open, double high, double low, double close, Decimal volume) {
}
