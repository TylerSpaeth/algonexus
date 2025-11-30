package com.github.tylerspaeth.broker;

import com.ib.client.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderState {

    public final int orderID;
    public final Contract contract;
    public final Order order;
    public volatile OrderStatus status = OrderStatus.PendingSubmit;
    public volatile int cumulativeFilled = 0;
    public volatile boolean execDetailsEnded = false;

    public final List<Execution> executions = Collections.synchronizedList(new ArrayList<>());
    public final List<CommissionAndFeesReport> commissions = Collections.synchronizedList(new ArrayList<>());

    public OrderState(int orderID, Contract contract, Order order) {
        this.orderID = orderID;
        this.contract = contract;
        this.order = order;
    }

    public synchronized void updateFromOrderStatus(int filled, int remaining, double lastFillPrice, OrderStatus newStatus) {
        status = newStatus;
        cumulativeFilled = filled;
    }

    public synchronized void addExecution(Execution e) {
        executions.add(e);
    }

    public synchronized void addCommission(CommissionAndFeesReport commissionAndFeesReport) {
        commissions.add(commissionAndFeesReport);
    }
}
