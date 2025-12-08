package com.github.tylerspaeth.broker.response;

public record PositionPnL(double position, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {}