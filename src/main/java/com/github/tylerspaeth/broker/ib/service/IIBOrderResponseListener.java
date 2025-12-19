package com.github.tylerspaeth.broker.ib.service;

import com.github.tylerspaeth.broker.ib.response.OrderResponse;

/**
 * Listener for subscribing to changes in an OrderResponseObject.
 */
public interface IIBOrderResponseListener {

    /**
     * To be called any time an OrderResponse is updated.
     * @param orderResponse The updated OrderResponse.
     */
    void update(OrderResponse orderResponse);

}
