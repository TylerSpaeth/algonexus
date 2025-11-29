package com.github.tylerspaeth.broker.response;

import com.ib.client.Decimal;

public record PositionPnLResponse(Decimal position, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {}
