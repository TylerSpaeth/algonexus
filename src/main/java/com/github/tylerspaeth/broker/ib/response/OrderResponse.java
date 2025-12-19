package com.github.tylerspaeth.broker.ib.response;

import com.github.tylerspaeth.broker.ib.IIBOrderResponseListener;
import com.ib.client.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderResponse {

    private IIBOrderResponseListener orderResponseListener;

    public final int orderID;
    public final Contract contract;
    public final Order order;

    public final List<com.github.tylerspaeth.broker.response.OrderStatus> statuses = new ArrayList<>();

    public volatile int cumulativeFilled = 0;
    private volatile boolean execDetailsEnded = false;

    public final List<Execution> executions = Collections.synchronizedList(new ArrayList<>());
    public final List<CommissionAndFeesReport> commissions = Collections.synchronizedList(new ArrayList<>());

    public OrderResponse(int orderID, Contract contract, Order order) {
        this.orderID = orderID;
        this.contract = contract;
        this.order = order;
        statuses.add(new com.github.tylerspaeth.broker.response.OrderStatus(OrderStatus.PendingSubmit, Timestamp.from(Instant.now())));
    }

    public synchronized void updateFromOrderStatus(int filled, int remaining, double lastFillPrice, com.ib.client.OrderStatus newStatus) {
        statuses.add(new com.github.tylerspaeth.broker.response.OrderStatus(newStatus, Timestamp.from(Instant.now())));
        cumulativeFilled = filled;
        orderResponseListener.update(this);
    }

    public synchronized void addExecution(Execution e) {
        executions.add(e);
        orderResponseListener.update(this);
    }

    public synchronized void addCommission(CommissionAndFeesReport commissionAndFeesReport) {
        commissions.add(commissionAndFeesReport);
        orderResponseListener.update(this);
    }

    /**
     * Sets the IIBOrderResponseListener that will consume any changes to this OrderResponse. An initial
     * update will be projected upon setting the listener.
     * @param orderResponseListener Listener that listens for OrderResponseChanges
     */
    public void setOrderResponseListener(IIBOrderResponseListener orderResponseListener) {
        this.orderResponseListener = orderResponseListener;
        orderResponseListener.update(this);
    }

    public boolean getExecDetailsEnded() {
        return execDetailsEnded;
    }

    public void setExecDetailsEnded(boolean execDetailsEnded) {
        this.execDetailsEnded = execDetailsEnded;
    }
}
