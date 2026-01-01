package com.github.tylerspaeth.engine.request.order;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

import java.util.List;

public class OpenOrdersRequest extends AbstractEngineRequest<List<Order>> {

    private final User user;

    public OpenOrdersRequest(User user) {
        this.user = user;
    }

    @Override
    protected List<Order> execute() {
        return orderService.getOpenOrders(user);
    }
}
