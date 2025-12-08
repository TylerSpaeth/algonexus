package com.github.tylerspaeth.broker.ib.response;

import com.ib.client.Decimal;

public record PositionPnL(Decimal position, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {}
