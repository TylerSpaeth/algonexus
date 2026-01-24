package com.github.tylerspaeth.broker.ib.service;

import com.github.tylerspaeth.broker.ib.IBMapper;
import com.github.tylerspaeth.broker.ib.IBOrderResponseProjector;
import com.github.tylerspaeth.broker.ib.OrderPersistor;
import com.github.tylerspaeth.broker.ib.response.OrderResponse;
import com.github.tylerspaeth.broker.service.IOrderService;
import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.User;
import com.ib.client.Contract;

import java.util.List;

public class IBOrderService implements IOrderService {

    private final IBSyncWrapper wrapper;

    private final OrderDAO orderDAO;

    public IBOrderService(OrderDAO orderDAO) {
        this.wrapper = IBSyncWrapper.getInstance();
        this.orderDAO = orderDAO;
    }

    @Override
    public Order placeOrder(long threadId, Order order) {
        Contract contract = new Contract();
        com.ib.client.Order ibOrder = new com.ib.client.Order();
        IBMapper.mapOrderToIBContractAndOrder(order, contract, ibOrder);
        OrderResponse orderResponse = wrapper.placeOrder(contract, ibOrder);
        orderResponse.setOrderResponseListener(new IBOrderResponseProjector(order, new OrderPersistor()));
        return order;
    }

    @Override
    public void cancelOrder(long threadID, Order order) {
        wrapper.cancelOrder(Integer.parseInt(order.getExternalOrderID()));
    }

    @Override
    public List<Order> getOpenOrders(User user) {
        return orderDAO.getOpenOrdersForUser(user);
    }
}
