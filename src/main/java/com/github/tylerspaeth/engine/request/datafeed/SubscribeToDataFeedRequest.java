package com.github.tylerspaeth.engine.request.datafeed;

import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class SubscribeToDataFeedRequest extends AbstractEngineRequest<Void> {

    private final long threadID;
    private final Symbol symbol;

    public SubscribeToDataFeedRequest(Symbol symbol) {
        this.symbol = symbol;
        threadID = Thread.currentThread().threadId();
    }

    @Override
    protected Void execute() {
        dataFeedService.subscribeToDataFeed(threadID, symbol);
        return null;
    }
}
