package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.common.data.dao.CommissionDAO;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.dao.TradeDAO;
import com.github.tylerspaeth.common.data.entity.*;
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

// Note that Trail limit orders are not perfectly simulated right now. We can not simulate the behavior that happens in
// the middle of the candlestick exactly. It could be reworked to incorporate RNG to determine the inner candle ordering.
// For now, it assumes that the trigger price will be changes before the limit is hit.

public class BacktesterSharedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterSharedService.class);

    private final OrderDAO orderDAO;
    private final TradeDAO tradeDAO;
    private final CommissionDAO commissionDAO;

    final Map<BacktesterDataFeedKey, Candlestick> lastSeenCandlesticks = new ConcurrentHashMap<>();
    final Map<BacktesterDataFeedKey, Timestamp> currentTimestamps = new ConcurrentHashMap<>();
    final Map<BacktesterDataFeedKey, Map<Integer, Order>> pendingOrders = new ConcurrentHashMap<>();

    public BacktesterSharedService(OrderDAO orderDAO, TradeDAO tradeDAO, CommissionDAO commissionDAO) {
        this.orderDAO = orderDAO;
        this.tradeDAO = tradeDAO;
        this.commissionDAO = commissionDAO;
    }

    /**
     * Updates s specific datafeed with the last seen candlestick and the current time the data feed it at.
     * @param mapKey BacktesterDataFeedKey for identifying the datafeed.
     * @param lastSeenCandlestick Candlestick that has most recently been seen in the datafeed.
     * @param currentTimestamp Timestamp representing the current time in the data feed.
     */
    public void updateDataFeed(BacktesterDataFeedKey mapKey, Candlestick lastSeenCandlestick, Timestamp currentTimestamp) {

        if(mapKey == null) {
            LOGGER.error("Attempted to update data feed with null key.");
            return;
        }

        if(lastSeenCandlestick == null || currentTimestamp == null) {
            LOGGER.error("Attempted to update data feed with null value. Candlestick: {} Timestamp: {}", lastSeenCandlestick, currentTimestamp);
            return;
        }

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
                            order.setPrice(Math.min(order.getPrice(), newPrice));
                        }
                    } else if(trailPercent != null) {
                        float newPrice = lastSeenCandlestick.getLow() * (1 + trailPercent);
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice );
                        } else {
                            order.setPrice(Math.min(order.getPrice(), newPrice));
                        }
                    }
                } else if(order.getSide() == SideEnum.SELL) {
                    if(trailAmount != null) {
                        float newPrice = lastSeenCandlestick.getHigh() - trailAmount;
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice);
                        } else {
                            order.setPrice(Math.max(order.getPrice(), newPrice));
                        }
                    } else if(trailPercent != null) {
                        float newPrice = lastSeenCandlestick.getHigh() * (1 - trailPercent);
                        if(order.getPrice() == null) {
                            order.setPrice(newPrice );
                        } else {
                            order.setPrice(Math.max(order.getPrice(), newPrice));
                        }
                    }
                }
            }

            for(Iterator<Order> it = pendingOrdersForMapKey.values().iterator(); it.hasNext();) {
                Order order = it.next();
                boolean orderFilled = tryToFillOrder(mapKey, order, false);
                if(orderFilled && order.getOCAGroup() != null && !order.getOCAGroup().isBlank()) {
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
        return mapKey != null && lastSeenCandlesticks.containsKey(mapKey) && currentTimestamps.containsKey(mapKey);
    }

    /**
     * Places an order off of the data feed corresponding to the provided key.
     * @param mapKey BacktesterDataFeedKey for the data feed.
     * @param order Order to be placed.
     */
    public void addOrder(BacktesterDataFeedKey mapKey, Order order) {

        if(mapKey == null) {
            LOGGER.error("Can not add order when key is null.");
            return;
        }

        if(order == null) {
            LOGGER.error("Can not add order when it is null");
            return;
        }

        try {
            order.validatePlaceable();
        } catch(IllegalStateException e) {
            LOGGER.error("Unable to place order as it is invalid.", e);
            return;
        }


        Timestamp currentTimestamp = currentTimestamps.get(mapKey);
        Candlestick lastSeenCandlestick = lastSeenCandlesticks.get(mapKey);

        if(currentTimestamp == null || lastSeenCandlestick == null) {
            LOGGER.error("Unable to add order, mapKey does not exist.");
            return;
        }

        order.setStatus(OrderStatusEnum.SUBMITTED);
        order.setTimePlaced(currentTimestamps.get(mapKey));
        orderDAO.update(order);

        // If the trail is supposed to act like a market order then set its price to the current price so it fills right away
        if(order.getOrderType() == OrderTypeEnum.TRL_LMT) {
            switch(order.getSide()) {
                case BUY -> {
                    if(order.getTrailAmount() != null) {
                        order.setPrice(lastSeenCandlestick.getClose() + order.getTrailAmount());
                    } else if(order.getTrailPercent() != null) {
                        order.setPrice(lastSeenCandlestick.getClose() * (1+order.getTrailPercent()));
                    }
                }
                case SELL -> {
                    if(order.getTrailAmount() != null) {
                        order.setPrice(lastSeenCandlestick.getClose() - order.getTrailAmount());
                    } else if(order.getTrailPercent() != null) {
                        order.setPrice(lastSeenCandlestick.getClose() * (1-order.getTrailPercent()));
                    }
                }
            }
        }

        Map<Integer, Order> pendingOrdersForMapKey = pendingOrders.computeIfAbsent(mapKey, _ -> new ConcurrentHashMap<>());
        pendingOrdersForMapKey.put(order.getOrderID(), order);

        boolean orderFilled = tryToFillOrder(mapKey, order, true);
        if(orderFilled) {
            if(order.getOCAGroup() != null && !order.getOCAGroup().isBlank()) {
                ocaGroupTriggered(mapKey, order.getOCAGroup());
            }
            pendingOrdersForMapKey.values().stream()
                    .filter(childOrder -> childOrder.getParentOrder() != null && childOrder.getParentOrder().equals(order))
                    .forEach(childOrder -> tryToFillOrder(mapKey, childOrder, true));
        }

        // Once a transmit flag is seen all other pending orders should have their flags updated
        if(order.isTransmit()) {
            pendingOrdersForMapKey.values().forEach(pendingOrder -> {
                if(!pendingOrder.isTransmit() && !order.isFinalized()) {
                    pendingOrder.setTransmit(true);
                    orderDAO.update(pendingOrder);

                    boolean pendingOrderFilled = tryToFillOrder(mapKey, pendingOrder, true);
                    if(pendingOrderFilled) {
                        if(pendingOrder.getOCAGroup() != null && !pendingOrder.getOCAGroup().isBlank()) {
                            ocaGroupTriggered(mapKey, pendingOrder.getOCAGroup());
                        }
                        pendingOrdersForMapKey.values().stream()
                                .filter(childOrder -> childOrder.getParentOrder() != null && childOrder.getParentOrder().equals(pendingOrder))
                                .forEach(childOrder -> tryToFillOrder(mapKey, childOrder, true));
                    }

                }
            });
        }

        pendingOrdersForMapKey.entrySet().removeIf(e -> e.getValue().isFinalized());
    }

    /**
     * Cancels the provided order.
     * @param mapKey BacktesterDataFeedKey for the data feed the order is placed on.
     * @param orderID Integer orderId of the order that needs to be canceled.
     */
    public void cancelOrder(BacktesterDataFeedKey mapKey, Integer orderID) {

        if(mapKey == null) {
            LOGGER.error("Can not cancel order when key is null.");
            return;
        }

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

        // If a parent is cancelled, cancel the children
        pendingOrdersForMapKey.values().stream()
                .filter(order1 -> order1.getParentOrder() == order)
                .forEach(order1 -> {
                    order1.setStatus(OrderStatusEnum.CANCELLED);
                    order1.setTimeClosed(currentTimestamps.get(mapKey));
                    order1.setFinalized(true);
                    orderDAO.update(order1);
        });
    }

    // TODO consider if orders should be fillable on the same candlestick they are created on
    // TODO simulate tif values

    /**
     * Attempts to fill the provided Order on the data feed corresponding to the given key.
     * @param mapKey BacktesterDataFeedKey
     * @param order Order to try filling.
     * @param newOrder If this is a new order that was just added, rather than trying to fill from a data feed update
     */
    private boolean tryToFillOrder(BacktesterDataFeedKey mapKey, Order order, boolean newOrder) {

        if(mapKey == null) {
            LOGGER.error("Can not try to fill order when key is null.");
            return false;
        }

        // Orders that can only be filled if they have transmit set to true
        if(!order.isTransmit()) {
            return false;
        }

        // Orders can not be filled until the parent order is filled
        if(order.getParentOrder() != null && order.getParentOrder().getStatus() != OrderStatusEnum.FILLED) {
            return false;
        }

        // Do not fill an order that has already been filled
        if (order.isFinalized() || order.getStatus() == OrderStatusEnum.FILLED) {
            return false;
        }

        Candlestick lastSeenCandlestick = lastSeenCandlesticks.get(mapKey);
        Timestamp currentTimestamp = currentTimestamps.get(mapKey);

        float candlestickLow = newOrder ? lastSeenCandlestick.getClose() : lastSeenCandlestick.getLow();
        float candlestickHigh = newOrder ? lastSeenCandlestick.getClose() : lastSeenCandlestick.getHigh();

        float limitFillPriceLow = newOrder ? candlestickLow : order.getPrice();
        float limitFillPriceHigh = newOrder ? candlestickHigh : order.getPrice();

        // If this is a new order then the only price with will be considered for filling is close price
        switch (order.getOrderType()) {
            case MKT:
                fillOrder(order, lastSeenCandlestick.getClose(), currentTimestamp);
                return true;
            case LMT:
                if(order.getSide() == SideEnum.BUY && candlestickLow <= order.getPrice()) {
                    fillOrder(order, limitFillPriceLow, currentTimestamp);
                    return true;
                } else if(order.getSide() == SideEnum.SELL && candlestickHigh >= order.getPrice()) {
                    fillOrder(order, limitFillPriceHigh, currentTimestamp);
                    return true;
                }
                break;
            case STP, STP_LMT:
                if(order.getSide() == SideEnum.BUY && candlestickHigh >= order.getPrice()) {
                    fillOrder(order, limitFillPriceHigh, currentTimestamp);
                    return true;
                } else if(order.getSide() == SideEnum.SELL && candlestickLow <= order.getPrice()) {
                    fillOrder(order, limitFillPriceLow, currentTimestamp);
                    return true;
                }
                break;
            case TRL_LMT:
                // For trail limits in the backtester, the price field on the order will store the current limit price
                if(order.getSide() == SideEnum.BUY && candlestickHigh >= order.getPrice()) {
                    float fillPrice = order.getPrice();
                    order.setPrice(null);
                    fillOrder(order, fillPrice, currentTimestamp);
                    return true;
                } else if(order.getSide() == SideEnum.SELL && candlestickLow <= order.getPrice()) {
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

        try {
            Symbol symbol = order.getSymbol();
            if(symbol.getCommission() != null) {
                // Using symbol specific commissions
                trade.setFees(symbol.getCommission().getCommissionAmount());
            } else {
                // Using default commissions for the asset type
                Commission commission = commissionDAO.findDefaultCommissionForAssetType(symbol.getAssetType());
                trade.setFees(commission.getCommissionAmount());
            }
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to get commission and fees when filling order. Defaulting to 0.", e);
            trade.setFees(0f);
        }

        trade.setOrder(order);
        order.getTrades().add(trade);
        order.setStatus(OrderStatusEnum.FILLED);
        order.setTimeClosed(timestamp);
        order.setFinalized(true);
        orderDAO.update(order);
        tradeDAO.insert(trade);
    }

    /**
     * When an order in an OCA group gets triggered this method gets called to cancel the rest of the orders in the group.
     * @param mapKey Key for the datafeed that this order was placed on.
     * @param ocaGroup String with OCAGroup name.
     */
    private void ocaGroupTriggered(BacktesterDataFeedKey mapKey, String ocaGroup) {

        if(mapKey == null) {
            LOGGER.error("Can not trigger oca group when key is null.");
            return;
        }

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
