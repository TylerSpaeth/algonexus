package com.github.tylerspaeth.broker.response;

public record AccountPnLResponse(double dailyPnL, double unrealizedPnL, double realizedPnL) {

}
