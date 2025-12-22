package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.broker.service.IOrderService;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.*;
import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BacktesterOrderService implements IOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterOrderService.class);

    private final BacktesterSharedService backtesterSharedService;

    private final OrderDAO orderDAO;
    private final SymbolDAO symbolDAO;

    public BacktesterOrderService(BacktesterSharedService backtesterSharedService, OrderDAO orderDAO, SymbolDAO symbolDAO) {
        this.backtesterSharedService = backtesterSharedService;
        this.orderDAO = orderDAO;
        this.symbolDAO = symbolDAO;
    }

    @Override
    public Order placeOrder(Order order) {

        if(order.getOrderID() != null) {
            LOGGER.error("This order already is persisted, unable to place a new order with it. {}", order);
            return null;
        }

        Symbol persistedSymbol = symbolDAO.getPersistedVersionOfSymbol(order.getSymbol());
        if(persistedSymbol == null) {
            LOGGER.error("Symbol {} is not persisted, therefore Order can not be placed.", order.getSymbol());
            return null;
        }

        BacktesterDataFeedKey mapKey = BacktesterDataFeedKey.createKeyForSymbol(persistedSymbol.getSymbolID());

        if(!backtesterSharedService.hasActiveDataFeed(mapKey)) {
            LOGGER.error("There must be an active data feed for this Symbol {} to be able to place an order.", order.getSymbol());
            return null;
        }

        try {
            order.validatePlaceable();
        } catch(IllegalStateException e) {
            LOGGER.error("Order placement failed", e);
            return null;
        }

        order.setStatus(OrderStatusEnum.PENDING_SUBMIT);

        Order mergedOrder = orderDAO.update(order);

        backtesterSharedService.addOrder(mapKey, mergedOrder);

        return mergedOrder;
    }

    @Override
    public void cancelOrder(Order order) {
        if(order.getOrderID() == null) {
            LOGGER.error("Unable to cancel order, it does not have an orderID.");
            return;
        }
        backtesterSharedService.cancelOrder(BacktesterDataFeedKey.createKeyForSymbol(order.getSymbol().getSymbolID()), order.getOrderID());
    }

    @Override
    public List<Order> getOpenOrders(User user) {
        // TODO look into whether this should be used of if we should check the pending orders in the shared service
        return orderDAO.getOpenOrdersForUser(user);
    }
}
