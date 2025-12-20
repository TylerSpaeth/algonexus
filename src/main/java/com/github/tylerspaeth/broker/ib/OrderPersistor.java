package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.entity.Order;

/**
 * Used for persisting Order changes to database.
 */
public class OrderPersistor implements IOrderListener {

    private final OrderDAO orderDAO;

    public OrderPersistor() {
        this.orderDAO = new OrderDAO();
    }

    @Override
    public Order update(Order order) {
        return orderDAO.update(order);
    }

}
