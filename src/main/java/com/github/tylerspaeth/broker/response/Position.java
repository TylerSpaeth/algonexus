package com.github.tylerspaeth.broker.response;

import com.github.tylerspaeth.common.data.entity.Symbol;

public record Position(Symbol symbol, double position, double avgCost) {}
