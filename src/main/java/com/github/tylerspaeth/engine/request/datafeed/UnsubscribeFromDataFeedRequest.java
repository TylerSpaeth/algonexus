package com.github.tylerspaeth.engine.request.datafeed;

import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class UnsubscribeFromDataFeedRequest extends AbstractEngineRequest<Void> {

    private final long threadID;
    private final Symbol symbol;

    public UnsubscribeFromDataFeedRequest(Symbol symbol) {
        this.symbol = symbol;
        this.threadID = Thread.currentThread().threadId();
    }

    @Override
    protected Void execute() {
        dataFeedService.unsubscribeFromDataFeed(threadID, symbol);
        return null;
    }
}
