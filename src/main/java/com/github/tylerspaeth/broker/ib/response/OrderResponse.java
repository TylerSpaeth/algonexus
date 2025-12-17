package com.github.tylerspaeth.broker.ib.response;

import com.ib.client.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderResponse {

    public final int orderID;
    public final Contract contract;
    public final Order order;

    public final List<com.github.tylerspaeth.broker.response.OrderStatus> statuses = new ArrayList<>();

    public volatile int cumulativeFilled = 0;
    public volatile boolean execDetailsEnded = false;

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
    }

    public synchronized void addExecution(Execution e) {
        executions.add(e);
    }

    public synchronized void addCommission(CommissionAndFeesReport commissionAndFeesReport) {
        commissions.add(commissionAndFeesReport);
    }
}
