package com.github.tylerspaeth.broker.ib.response;

public record AccountSummary(String accountID, String tag, String value, String currency) {}
