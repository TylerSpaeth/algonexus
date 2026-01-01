package com.github.tylerspaeth.engine.request.order;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class PlaceOrderRequest extends AbstractEngineRequest<Order> {

    private final Order order;

    public PlaceOrderRequest(Order order) {
        this.order = order;
    }

    @Override
    protected Order execute() {
        return orderService.placeOrder(order);
    }
}
