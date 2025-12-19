package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.ib.response.OrderResponse;
import com.github.tylerspaeth.broker.ib.service.IIBOrderResponseListener;
import com.github.tylerspaeth.broker.response.OrderStatus;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.OrderEvent;
import com.github.tylerspaeth.common.data.entity.Trade;
import com.github.tylerspaeth.common.enums.SideEnum;
import com.ib.client.CommissionAndFeesReport;
import com.ib.client.Execution;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Projects changes on an OrderResponse object to an Order object.
 */
public class IBOrderResponseProjector implements IIBOrderResponseListener {

    private final Order order;

    public IBOrderResponseProjector(Order order) {
        this.order = order;
    }

    @Override
    public void update(OrderResponse orderResponse) {
        synchronized (order) {
            if (order.getExternalOrderID() == null) {
                order.setExternalOrderID(String.valueOf(orderResponse.order.permId()));
            }
            for (Execution execution : orderResponse.executions) {
                Optional<Trade> matchedTrade = order.getTrades().stream().filter(trade -> trade.getExternalTradeID().equals(String.valueOf(execution.execId()))).findFirst();

                // If this Execution has not been added as a trade, do so.
                if (matchedTrade.isEmpty()) {
                    Trade trade = new Trade();
                    trade.setSide(SideEnum.valueOf(execution.side()));
                    trade.setTimestamp(Timestamp.valueOf(execution.time()));
                    trade.setExternalTradeID(execution.execId());
                    trade.setFillPrice((float) execution.price());
                    trade.setFillQuantity(execution.cumQty().value().floatValue());
                    trade.setOrder(order);
                    order.getTrades().add(trade);
                    matchedTrade = Optional.of(trade);
                }

                // If the Trade has not yet had fees assigned, try to find them and update.
                if (matchedTrade.get().getFees() == null) {
                    Optional<CommissionAndFeesReport> matchedComm = orderResponse.commissions.stream().filter(commissionAndFeesReport -> commissionAndFeesReport.execId().equals(execution.execId())).findFirst();
                    if (matchedComm.isPresent()) {
                        matchedTrade.get().setFees((float) matchedComm.get().commissionAndFees());
                    }
                }
            }

            // Map any needed order statuses
            for (OrderStatus status : orderResponse.statuses) {
                boolean alreadyAdded = order.getOrderEvents().stream().anyMatch(orderEvent -> orderEvent.getTimestamp().equals(status.timestamp()) && orderEvent.getNewStatus().equals(IBMapper.mapOrderStatus(status.orderStatus())));

                if (!alreadyAdded) {
                    OrderEvent orderEvent = new OrderEvent();
                    orderEvent.setNewStatus(IBMapper.mapOrderStatus(status.orderStatus()));
                    orderEvent.setTimestamp(status.timestamp());
                    order.getOrderEvents().add(orderEvent);
                    orderEvent.setOrder(order);
                }
            }

            // Set the order status to the most recent status
            if (!order.getOrderEvents().isEmpty()) {
                OrderEvent mostRecent = order.getOrderEvents().getFirst();

                for (OrderEvent orderEvent : order.getOrderEvents()) {
                    if (orderEvent.getTimestamp().after(mostRecent.getTimestamp())) {
                        mostRecent = orderEvent;
                    }
                }

                order.setStatus(mostRecent.getNewStatus());
            }

            // Finalize the order if we are not receiving more executions
            if(orderResponse.getExecDetailsEnded()) {
                order.setFinalized(true);
            }
        }
    }
}
