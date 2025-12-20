package com.github.tylerspaeth.broker.service;

import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.User;

import java.util.List;

/**
 * Order management functionality
 */
public interface IOrderService {

    /**
     * Places an order with the provided Order.
     * @param order Order object describing the desired order.
     * @return Order that should only be read from.
     */
    Order placeOrder(Order order);

    /**
     * Cancels the provided Order.
     * @param order The order that needs to be canceled.
     */
    void cancelOrder(Order order);

    /**
     * Get open (non-finalized) orders for the provided user.
     * @param user User to get open orders for.
     * @return List of Orders.
     */
    List<Order> getOpenOrders(User user);

}
