package com.github.tylerspaeth.broker.response;

public record AccountSummary(String accountID,
                             Double availableFunds,
                             Double excessLiquidity,
                             Double buyingPower,
                             Double maintMarginReq,
                             Double settledCash,
                             Double grossPositionValue,
                             Double totalCashValue) {}
