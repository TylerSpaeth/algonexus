package com.github.tylerspaeth.engine.request.datafeed;

import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

import java.util.List;

public class ReadFromDataFeedRequest extends AbstractEngineRequest<List<Candlestick>> {

    private final long threadID;
    private final Symbol symbol;
    private final int intervalDuration;
    private final IntervalUnitEnum intervalUnit;

    public ReadFromDataFeedRequest(Symbol symbol, int intervalDuration, IntervalUnitEnum intervalUnit) {
        this.symbol = symbol;
        this.intervalDuration = intervalDuration;
        this.intervalUnit = intervalUnit;
        this.threadID = Thread.currentThread().threadId();
    }

    @Override
    protected List<Candlestick> execute() {
        return dataFeedService.readFromDataFeed(threadID, symbol, intervalDuration, intervalUnit);
    }
}
