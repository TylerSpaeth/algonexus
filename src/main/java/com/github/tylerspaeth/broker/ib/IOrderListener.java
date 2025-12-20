package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.common.data.entity.Order;

/**
 * Listener for activity on Orders.
 */
public interface IOrderListener {

    /**
     * Called when there is an update to the Order.
     * @param order Order that has been updated.
     * @return The provided Order, possibly changed.
     */
    Order update(Order order);
}
