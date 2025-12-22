package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.Order;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import com.github.tylerspaeth.common.enums.OrderTypeEnum;
import com.github.tylerspaeth.common.enums.SideEnum;
import com.github.tylerspaeth.common.enums.TimeInForceEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class BacktesterOrderServiceTest {

    @Mock
    private OrderDAO orderDAO;
    @Mock
    private SymbolDAO symbolDAO;
    @Mock
    private BacktesterSharedService backtesterSharedService;

    private BacktesterOrderService backtesterOrderService;

    @BeforeEach
    public void setup() {
        backtesterOrderService = new BacktesterOrderService(backtesterSharedService, orderDAO, symbolDAO);
    }

    @Test
    public void testPlaceOrderWithOrderIDReturnsNull() throws Exception {
        Order order = new Order();
        order.setPrice(1f);
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSymbol(new Symbol());
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);
        order.setTimeInForce(TimeInForceEnum.GTC);

        Assertions.assertNull(backtesterOrderService.placeOrder(order));
    }

    @Test
    public void testPlaceOrderWithNonPersistedSymbolReturnsNull() {
        Order order = new Order();
        order.setPrice(1f);
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSymbol(new Symbol());
        order.setTimeInForce(TimeInForceEnum.GTC);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(null);

        Assertions.assertNull(backtesterOrderService.placeOrder(order));
    }

    @Test
    public void testPlaceOrderWithoutDataFeedReturnsNull() {
        Order order = new Order();
        order.setPrice(1f);
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSymbol(new Symbol());
        order.setTimeInForce(TimeInForceEnum.GTC);

        try(MockedStatic<BacktesterDataFeedKey> mockedStatic = Mockito.mockStatic(BacktesterDataFeedKey.class)) {
            mockedStatic.when(() -> BacktesterDataFeedKey.createKeyForSymbol(Mockito.isNull())).thenReturn(new BacktesterDataFeedKey(1,1));
            when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(new Symbol());
            when(backtesterSharedService.hasActiveDataFeed(Mockito.any(BacktesterDataFeedKey.class))).thenReturn(false);

            Assertions.assertNull(backtesterOrderService.placeOrder(order));
        }
    }

    @Test
    public void testPlacingNonPlaceableOrderReturnsNull() {
        Order order = new Order();
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.LMT);
        order.setSymbol(new Symbol());

        try(MockedStatic<BacktesterDataFeedKey> mockedStatic = Mockito.mockStatic(BacktesterDataFeedKey.class)) {
            mockedStatic.when(() -> BacktesterDataFeedKey.createKeyForSymbol(Mockito.isNull())).thenReturn(new BacktesterDataFeedKey(1,1));
            when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(new Symbol());
            when(backtesterSharedService.hasActiveDataFeed(Mockito.any(BacktesterDataFeedKey.class))).thenReturn(true);

            Assertions.assertNull(backtesterOrderService.placeOrder(order));
        }
    }

    @Test
    public void testPlaceValidOrder() {
        Order order = new Order();
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSymbol(new Symbol());
        order.setTimeInForce(TimeInForceEnum.GTC);

        try(MockedStatic<BacktesterDataFeedKey> mockedStatic = Mockito.mockStatic(BacktesterDataFeedKey.class)) {
            mockedStatic.when(() -> BacktesterDataFeedKey.createKeyForSymbol(Mockito.isNull())).thenReturn(new BacktesterDataFeedKey(1,1));
            when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(new Symbol());
            when(backtesterSharedService.hasActiveDataFeed(Mockito.any(BacktesterDataFeedKey.class))).thenReturn(true);
            when(orderDAO.update(Mockito.any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

            Assertions.assertEquals(OrderStatusEnum.PENDING_SUBMIT, backtesterOrderService.placeOrder(order).getStatus());
            verify(backtesterSharedService, Mockito.times(1)).addOrder(Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void testCancelOrderWithoutIDDoesNothing() {
        Order order = new Order();
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSymbol(new Symbol());
        order.setTimeInForce(TimeInForceEnum.GTC);

        Assertions.assertDoesNotThrow(() -> backtesterOrderService.cancelOrder(order));
    }

    @Test
    public void testCancelOrderWithIDDoesNothing() throws Exception {
        Order order = new Order();
        order.setQuantity(2f);
        order.setUser(new User());
        order.setSide(SideEnum.BUY);
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSymbol(new Symbol());
        order.setTimeInForce(TimeInForceEnum.GTC);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        Assertions.assertDoesNotThrow(() -> backtesterOrderService.cancelOrder(order));
        verify(backtesterSharedService, Mockito.times(1)).cancelOrder(Mockito.any(), Mockito.any());
    }

}
