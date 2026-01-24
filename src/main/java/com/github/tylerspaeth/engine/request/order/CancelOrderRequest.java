package com.github.tylerspaeth.engine.request.order;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class CancelOrderRequest extends AbstractEngineRequest<Void> {

    private final Order order;
    private final long threadID;

    public CancelOrderRequest(Order order) {
        this.order = order;
        this.threadID = Thread.currentThread().threadId();
    }

    @Override
    protected Void execute() {
        orderService.cancelOrder(threadID, order);
        return null;
    }
}
