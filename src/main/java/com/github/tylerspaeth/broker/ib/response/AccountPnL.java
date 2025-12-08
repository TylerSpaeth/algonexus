package com.github.tylerspaeth.broker.ib.response;

public record AccountPnL(double dailyPnL, double unrealizedPnL, double realizedPnL) {}
