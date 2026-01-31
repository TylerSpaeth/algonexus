package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.broker.service.IOrderService;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class BacktesterOrderService implements IOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterOrderService.class);

    private final BacktesterSharedService backtesterSharedService;

    private final OrderDAO orderDAO;
    private final SymbolDAO symbolDAO;

    private final Map<BacktesterDataFeedKey, Object> datafeedLocks;

    public BacktesterOrderService(BacktesterSharedService backtesterSharedService, OrderDAO orderDAO, SymbolDAO symbolDAO, Map<BacktesterDataFeedKey, Object> datafeedLocks) {
        this.backtesterSharedService = backtesterSharedService;
        this.orderDAO = orderDAO;
        this.symbolDAO = symbolDAO;
        this.datafeedLocks = datafeedLocks;

    }

    @Override
    public Order placeOrder(long threadID, Order order) {
        if (order.getOrderID() != null) {
            LOGGER.error("This order already is persisted, unable to place a new order with it. {}", order);
            return null;
        }

        Symbol persistedSymbol = symbolDAO.getPersistedVersionOfSymbol(order.getSymbol());
        if (persistedSymbol == null) {
            LOGGER.error("Symbol {} is not persisted, therefore Order can not be placed.", order.getSymbol());
            return null;
        }

        BacktesterDataFeedKey mapKey = new BacktesterDataFeedKey(persistedSymbol.getSymbolID(), threadID);

        Object datafeedLock = datafeedLocks.computeIfAbsent(mapKey, _ -> new Object());

        synchronized (datafeedLock) {
            if (!backtesterSharedService.hasActiveDataFeed(mapKey)) {
                LOGGER.error("There must be an active data feed for this Symbol {} to be able to place an order.", order.getSymbol());
                return null;
            }

            try {
                order.validatePlaceable();
            } catch (IllegalStateException e) {
                LOGGER.error("Order placement failed", e);
                return null;
            }

            order.setStatus(OrderStatusEnum.PENDING_SUBMIT);
            order.setTimePlaced(Timestamp.from(Instant.ofEpochSecond(1)));

            orderDAO.update(order);
            backtesterSharedService.addOrder(mapKey, order);
            return order;
        }
    }

    @Override
    public void cancelOrder(long threadID, Order order) {
        if(order.getOrderID() == null) {
            LOGGER.error("Unable to cancel order, it does not have an orderID.");
            return;
        }

        BacktesterDataFeedKey mapKey = new BacktesterDataFeedKey(order.getSymbol().getSymbolID(), threadID);

        Object datafeedLock = datafeedLocks.computeIfAbsent(mapKey, _ -> new Object());

        synchronized (datafeedLock) {
            backtesterSharedService.cancelOrder(new BacktesterDataFeedKey(order.getSymbol().getSymbolID(), threadID), order.getOrderID());
        }
    }

    @Override
    public List<Order> getOpenOrders(User user) {
        // TODO look into whether this should be used of if we should check the pending orders in the shared service
        return orderDAO.getOpenOrdersForUser(user);
    }
}
