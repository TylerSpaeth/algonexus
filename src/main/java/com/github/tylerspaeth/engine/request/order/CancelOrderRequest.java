package com.github.tylerspaeth.engine.request.order;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class CancelOrderRequest extends AbstractEngineRequest<Void> {

    private final Order order;

    public CancelOrderRequest(Order order) {
        this.order = order;
    }

    @Override
    protected Void execute() {
        orderService.cancelOrder(order);
        return null;
    }
}
