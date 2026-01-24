package com.github.tylerspaeth.engine.request.order;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class PlaceOrderRequest extends AbstractEngineRequest<Order> {

    private final Order order;
    private final long threadID;

    public PlaceOrderRequest(Order order) {
        this.order = order;
        this.threadID = Thread.currentThread().threadId();
    }

    @Override
    protected Order execute() {
        return orderService.placeOrder(threadID, order);
    }
}
