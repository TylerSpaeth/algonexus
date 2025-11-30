package com.github.tylerspaeth.broker;

import com.github.tylerspaeth.broker.response.OrderResponse;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderCancel;

public interface IOrderService {

    /**
     * Places an order and returns an object to monitor the state of the order.
     * @param contract The contract to open the order on
     * @param order The actual details of the order to open
     * @return OrderResponse object with details of the order's current state
     */
    OrderResponse placeOrder(Contract contract, Order order);

    /**
     * Cancels an order.
     * @param orderID The id of the order to cancel.
     * @return OrderCancel object with details of order's cancellation
     */
    OrderCancel cancelOrder(int orderID);

}
