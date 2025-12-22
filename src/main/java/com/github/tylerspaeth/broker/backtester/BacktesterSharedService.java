package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.Trade;
import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import com.github.tylerspaeth.common.enums.OrderTypeEnum;
import com.github.tylerspaeth.common.enums.SideEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BacktesterSharedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterSharedService.class);

    private final OrderDAO orderDAO;

    private final Map<BacktesterDataFeedKey, Candlestick> lastSeenCandlesticks = new ConcurrentHashMap<>();
    private final Map<BacktesterDataFeedKey, Timestamp> currentTimestamps = new ConcurrentHashMap<>();
    private final Map<BacktesterDataFeedKey, Map<Integer, Order>> pendingOrders = new ConcurrentHashMap<>();

    public BacktesterSharedService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * Updates s specific datafeed with the last seen candlestick and the current time the data feed it at.
     * @param mapKey BacktesterDataFeedKey for identifying the datafeed.
     * @param lastSeenCandlestick Candlestick that has most recently been seen in the datafeed.
     * @param currentTimestamp Timestamp representing the current time in the data feed.
     */
    public void updateDataFeed(BacktesterDataFeedKey mapKey, Candlestick lastSeenCandlestick, Timestamp currentTimestamp) {
        lastSeenCandlesticks.put(mapKey, lastSeenCandlestick);
        currentTimestamps.put(mapKey, currentTimestamp);
        Map<Integer, Order> pendingOrdersForMapKey = pendingOrders.get(mapKey);
        if(pendingOrdersForMapKey != null) {

            // For the backtester we set the price value on TRL_LMT orders to the price that will trigger the fill
            for(Order order : pendingOrdersForMapKey.values().stream().filter(order -> order.getOrderType() == OrderTypeEnum.TRL_LMT).collect(Collectors.toSet())) {

                Float trailAmount = order.getTrailAmount();
                Float trailPercent = order.getTrailPercent();

                if(order.getSide() == SideEnum.BUY) {
                    if(trailAmount != null) {
                        float newPrice = lastSeenCandlestick.getLow() + trailAmount;
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice);
                        } else {
                            order.setPrice(Math.max(order.getPrice(), newPrice));
                        }
                    } else if(trailPercent != null) {
                        float newPrice = lastSeenCandlestick.getLow() * (1 + trailPercent);
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice );
                        } else {
                            order.setPrice(Math.max(order.getPrice(), newPrice));
                        }
                    }
                } else if(order.getSide() == SideEnum.SELL) {
                    if(trailAmount != null) {
                        float newPrice = lastSeenCandlestick.getHigh() - trailAmount;
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice);
                        } else {
                            order.setPrice(Math.min(order.getPrice(), newPrice));
                        }
                    } else if(trailPercent != null) {
                        float newPrice = lastSeenCandlestick.getHigh() * (1 - trailPercent);
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice );
                        } else {
                            order.setPrice(Math.min(order.getPrice(), newPrice));
                        }
                    }
                }
            }

            for(Iterator<Order> it = pendingOrdersForMapKey.values().iterator(); it.hasNext();) {
                Order order = it.next();
                boolean filled = tryToFillOrder(mapKey, order);
                if(filled && !order.getOCAGroup().isBlank()) {
                    ocaGroupTriggered(mapKey, order.getOCAGroup());
                }
            }

            pendingOrdersForMapKey.entrySet().removeIf(e -> e.getValue().isFinalized());

        }
    }

    /**
     * Checks if there is an active data feed for the given key.
     * @param mapKey BacktesterDataFeedKey
     * @return true if there is an active data feed for the key, false otherwise.
     */
    public boolean hasActiveDataFeed(BacktesterDataFeedKey mapKey) {
        return lastSeenCandlesticks.containsKey(mapKey) && currentTimestamps.containsKey(mapKey);
    }

    /**
     * Places an order off of the data feed corresponding to the provided key.
     * @param mapKey BacktesterDataFeedKey for the data feed.
     * @param order Order to be placed.
     */
    public void addOrder(BacktesterDataFeedKey mapKey, Order order) {
        Timestamp currentTimestamp = currentTimestamps.get(mapKey);
        Candlestick lastSeenCandlestick = lastSeenCandlesticks.get(mapKey);

        if(currentTimestamp == null || lastSeenCandlestick == null) {
            LOGGER.error("Unable to add order, mapKey does not exist.");
            return;
        }

        order.setStatus(OrderStatusEnum.SUBMITTED);
        order.setTimePlaced(currentTimestamps.get(mapKey));
        orderDAO.update(order);

        Map<Integer, Order> pendingOrdersForMapKey = pendingOrders.computeIfAbsent(mapKey, _ -> new ConcurrentHashMap<>());
        pendingOrdersForMapKey.put(order.getOrderID(), order);

        boolean filled = tryToFillOrder(mapKey, order);
        if(filled && !order.getOCAGroup().isBlank()) {
            ocaGroupTriggered(mapKey, order.getOCAGroup());
        }

        pendingOrdersForMapKey.entrySet().removeIf(e -> e.getValue().isFinalized());
    }

    /**
     * Cancels the provided order.
     * @param mapKey BacktesterDataFeedKey for the data feed the order is placed on.
     * @param orderID Integer orderId of the order that needs to be canceled.
     */
    public void cancelOrder(BacktesterDataFeedKey mapKey, Integer orderID) {
        Map<Integer, Order> pendingOrdersForMapKey = pendingOrders.get(mapKey);

        if(pendingOrdersForMapKey == null) {
            LOGGER.error("Can not cancel order, mapKey does not exist.");
            return;
        }

        Order order = pendingOrdersForMapKey.remove(orderID);

        if(order == null) {
            LOGGER.error("Unable to cancel order, it does not exist in pendingOrders.");
            return;
        }

        // The price on TRL_LMT orders is used to track the price it will fill at, but it should not be persisted
        if(order.getOrderType() == OrderTypeEnum.TRL_LMT) {
            order.setPrice(null);
        }

        order.setStatus(OrderStatusEnum.CANCELLED);
        order.setTimeClosed(currentTimestamps.get(mapKey));
        order.setFinalized(true);
        orderDAO.update(order);
    }

    // TODO consider if orders should be fillable on the same candlestick they are created on

    /**
     * Attempts to fill the provided Order on the data feed corresponding to the given key.
     * @param mapKey BacktesterDataFeedKey
     * @param order Order to try filling.
     * @return true if the fill succeeds, false otherwise.
     */
    private boolean tryToFillOrder(BacktesterDataFeedKey mapKey, Order order) {

        // Orders that are in OCA groups can only be filled if the one of the order in the group has lastInOCAGroup has been set
        if(!order.getOCAGroup().isBlank() && !order.getLastInOCAGroup()) {
            boolean lastInGroupSeen = pendingOrders.get(mapKey).values().stream()
                    .filter(order1 -> order.getOCAGroup().equals(order1.getOCAGroup()))
                    .anyMatch(Order::getLastInOCAGroup);
            if(!lastInGroupSeen) {
                return false;
            }
        }

        Candlestick lastSeenCandlestick = lastSeenCandlesticks.get(mapKey);
        Timestamp currentTimestamp = currentTimestamps.get(mapKey);

        switch (order.getOrderType()) {
            case MKT:
                fillOrder(order, lastSeenCandlestick.getClose(), currentTimestamp);
                return true;
            case LMT:
                if(order.getSide() == SideEnum.BUY && lastSeenCandlestick.getLow() <= order.getPrice()) {
                    fillOrder(order, order.getPrice(), currentTimestamp);
                    return true;
                } else if(order.getSide() == SideEnum.SELL && lastSeenCandlestick.getHigh() >= order.getPrice()) {
                    fillOrder(order, order.getPrice(), currentTimestamp);
                    return true;
                }
                break;
            case STP, STP_LMT:
                if(order.getSide() == SideEnum.BUY && lastSeenCandlestick.getHigh() >= order.getPrice()) {
                    fillOrder(order, order.getPrice(), currentTimestamp);
                    return true;
                } else if(order.getSide() == SideEnum.SELL && lastSeenCandlestick.getLow() <= order.getPrice()) {
                    fillOrder(order, order.getPrice(), currentTimestamp);
                    return true;
                }
                break;
            case TRL_LMT:
                // For trail limits in the backtester, the price field on the order will store the current limit price
                if(order.getSide() == SideEnum.BUY && lastSeenCandlestick.getLow() <= order.getPrice()) {
                    float fillPrice = order.getPrice();
                    order.setPrice(null);
                    fillOrder(order, fillPrice, currentTimestamp);
                    return true;
                } else if(order.getSide() == SideEnum.SELL && lastSeenCandlestick.getHigh() >= order.getPrice()) {
                    float fillPrice = order.getPrice();
                    order.setPrice(null);
                    fillOrder(order, fillPrice, currentTimestamp);
                    return true;
                }
                break;
            case MOC:
                throw new RuntimeException("MOC orders are not currently supported for backtesting.");
            case LOC:
                throw new RuntimeException("LOC orders are not currently supported for backtesting.");
        }

        if(!order.getOCAGroup().isBlank() && order.getLastInOCAGroup()) {
            List<Order> ordersInOCAGroup = pendingOrders.get(mapKey).values().stream().filter(order1 -> order.getOCAGroup().equals(order1.getOCAGroup()) && !order1.getLastInOCAGroup()).toList();
            for(Order order1 : ordersInOCAGroup) {
                if(tryToFillOrder(mapKey, order1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Fills an order, adding a Trade to it.
     * @param order Order that needs to be filled.
     * @param price Price to fill the order at.
     * @param timestamp Time in which the order is filled.
     */
    private void fillOrder(Order order, float price, Timestamp timestamp) {
        Trade trade = new Trade();
        trade.setSide(order.getSide());
        trade.setFillPrice(price);
        trade.setFillQuantity(order.getQuantity());
        trade.setTimestamp(timestamp);
        trade.setFees(0f);
        trade.setOrder(order);
        order.getTrades().add(trade);
        orderDAO.update(order);
    }

    /**
     * When an order in an OCA group gets triggered this method gets called to cancel the rest of the orders in the group.
     * @param mapKey Key for the datafeed that this order was placed on.
     * @param ocaGroup String with OCAGroup name.
     */
    private void ocaGroupTriggered(BacktesterDataFeedKey mapKey, String ocaGroup) {
        List<Order> ordersInGroup = pendingOrders.get(mapKey).values().stream().filter(order -> ocaGroup.equals(order.getOCAGroup()) && order.getStatus() != OrderStatusEnum.FILLED).toList();
        for(Order order : ordersInGroup) {

            // The price on TRL_LMT orders is used to track the price it will fill at, but it should not be persisted
            if(order.getOrderType() == OrderTypeEnum.TRL_LMT) {
                order.setPrice(null);
            }

            order.setStatus(OrderStatusEnum.CANCELLED);
            order.setFinalized(true);
            orderDAO.update(order);
        }
    }

}
