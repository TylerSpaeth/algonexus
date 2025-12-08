package com.github.tylerspaeth.broker.ib.response;

import com.ib.client.Contract;
import com.ib.client.Decimal;

public record Position(Contract contract, Decimal position, double avgCost) {}
