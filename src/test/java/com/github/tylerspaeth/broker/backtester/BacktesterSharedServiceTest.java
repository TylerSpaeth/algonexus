package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.common.data.dao.OrderDAO;
import com.github.tylerspaeth.common.data.entity.Candlestick;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Timestamp;

@ExtendWith({MockitoExtension.class})
public class BacktesterSharedServiceTest {

    @Mock
    private OrderDAO orderDAO;

    private BacktesterSharedService backtesterSharedService;

    @BeforeEach
    public void setup() {
        backtesterSharedService = new BacktesterSharedService(orderDAO);
    }

    @Test
    public void testUpdateDataFeedWithNullKeyDoesNotThrow() {
        Assertions.assertDoesNotThrow(() -> backtesterSharedService.updateDataFeed(null, new Candlestick(), new Timestamp(1)));
    }

    @Test
    public void testUpdateDataFeedWithNullCandlestickDoesNotUpdate() {
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), null, new Timestamp(1));
        Assertions.assertEquals(0, backtesterSharedService.lastSeenCandlesticks.size());
        Assertions.assertEquals(0, backtesterSharedService.currentTimestamps.size());
    }

    @Test
    public void testUpdateDataFeedWithNullTimestampDoesNotUpdate() {
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), null);
        Assertions.assertEquals(0, backtesterSharedService.lastSeenCandlesticks.size());
        Assertions.assertEquals(0, backtesterSharedService.currentTimestamps.size());
    }

    @Test
    public void testUpdateDataFeedWithNullCandlestickAndTimestampDoesNotUpdate() {
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), null, null);
        Assertions.assertEquals(0, backtesterSharedService.lastSeenCandlesticks.size());
        Assertions.assertEquals(0, backtesterSharedService.currentTimestamps.size());
    }

    @Test
    public void testUpdateDataFeedCreatesNewDataFeedIfNotExist() {
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), new Timestamp(1));
        Assertions.assertEquals(1, backtesterSharedService.currentTimestamps.size());
        Assertions.assertEquals(1, backtesterSharedService.lastSeenCandlesticks.size());
    }

    @Test
    public void testUpdateDataFeedUpdatesDataFeedIfExists() {
        Candlestick updated = new Candlestick();
        updated.setHigh(100f);
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), new Timestamp(1));
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), updated, new Timestamp(5));
        Assertions.assertEquals(1, backtesterSharedService.currentTimestamps.size());
        Assertions.assertEquals(1, backtesterSharedService.lastSeenCandlesticks.size());
        Assertions.assertEquals(100, backtesterSharedService.lastSeenCandlesticks.get(BacktesterDataFeedKey.createKeyForSymbol(1)).getHigh());
        Assertions.assertEquals(new Timestamp(5), backtesterSharedService.currentTimestamps.get(BacktesterDataFeedKey.createKeyForSymbol(1)));
    }

    @Test
    public void testUpdateOtherDataFeedDoesNotOverwrite() {
        Candlestick updated = new Candlestick();
        updated.setHigh(100f);
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), new Timestamp(1));
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(2), updated, new Timestamp(5));
        Assertions.assertEquals(2, backtesterSharedService.currentTimestamps.size());
        Assertions.assertEquals(2, backtesterSharedService.lastSeenCandlesticks.size());
    }

    @Test
    public void testHasActiveDataFeedReturnsTrueWhenExpected() {
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), new Timestamp(1));
        Assertions.assertTrue(backtesterSharedService.hasActiveDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1)));
    }

    @Test
    public void testHasActiveDataFeedReturnsFalseWhenExpected() {
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), new Timestamp(1));
        Assertions.assertFalse(backtesterSharedService.hasActiveDataFeed(BacktesterDataFeedKey.createKeyForSymbol(2)));
    }

    @Test
    public void testAddOrderWithNullKeyDoesNotThrow() {
       Assertions.assertDoesNotThrow(() -> backtesterSharedService.addOrder(null, new Order()));
    }

    @Test
    public void tesAddOrderWithNullOrderDoesNotThrow() {
        Assertions.assertDoesNotThrow(() -> backtesterSharedService.addOrder(BacktesterDataFeedKey.createKeyForSymbol(1), null));
    }

    @Test
    public void testAddOrderWithoutDataFeedDoesNothing() {
        backtesterSharedService.addOrder(BacktesterDataFeedKey.createKeyForSymbol(1), new Order());
        Assertions.assertEquals(0, backtesterSharedService.pendingOrders.size());
    }

    @Test
    public void testAddInvalidOrderDoesNothing() {
        Order order = new Order();
        backtesterSharedService.updateDataFeed(BacktesterDataFeedKey.createKeyForSymbol(1), new Candlestick(), new Timestamp(1));
        backtesterSharedService.addOrder(BacktesterDataFeedKey.createKeyForSymbol(1), order);
        Assertions.assertEquals(0, backtesterSharedService.pendingOrders.size());
        Assertions.assertNull(order.getStatus());
    }

    private Symbol loadDataAndCreateSymbol(int symbolID) throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Timestamp timestamp = new Timestamp(0);
        Candlestick candlestick = new Candlestick();
        candlestick.setOpen(1f);
        candlestick.setHigh(5f);
        candlestick.setLow(0f);
        candlestick.setClose(3f);
        candlestick.setVolume(1000f);
        candlestick.setTimestamp(timestamp);
        backtesterSharedService.updateDataFeed(key, candlestick, timestamp);

        Symbol symbol = new Symbol();
        Field symbolIDField = symbol.getClass().getDeclaredField("symbolID");
        symbolIDField.setAccessible(true);
        symbolIDField.set(symbol, 1);

        return symbol;
    }

    @Test
    public void testAddMarketOrderFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);

        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(1f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(1, order.getTrades().getFirst().getFillQuantity());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddLimitOrderAsExactPriceFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(3f);
        order.setOrderType(OrderTypeEnum.LMT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(4, order.getTrades().getFirst().getFillQuantity());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddLimitBuyBelowPriceDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(1f);
        order.setOrderType(OrderTypeEnum.LMT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddLimitBuyAbovePriceFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(4f);
        order.setOrderType(OrderTypeEnum.LMT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddLimitSellBelowPriceFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(2f);
        order.setOrderType(OrderTypeEnum.LMT);
        order.setSide(SideEnum.SELL);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddLimitSellAbovePriceDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(4f);
        order.setOrderType(OrderTypeEnum.LMT);
        order.setSide(SideEnum.SELL);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddStopBuyBelowPriceFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(2f);
        order.setOrderType(OrderTypeEnum.STP);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddStopBuyAbovePriceDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(100f);
        order.setOrderType(OrderTypeEnum.STP);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddStopSellAbovePriceFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(100f);
        order.setOrderType(OrderTypeEnum.STP);
        order.setSide(SideEnum.SELL);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddStopSellBelowPriceDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setPrice(2f);
        order.setOrderType(OrderTypeEnum.STP);
        order.setSide(SideEnum.SELL);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddTrailLimitOfZeroAmountFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.TRL_LMT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        order.setTrailAmount(0f);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddTrailLimitOfZeroPercentFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.TRL_LMT);
        order.setSide(SideEnum.SELL);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        order.setTrailPercent(0f);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddTrailLimitAmountDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.TRL_LMT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        order.setTrailAmount(0.001f);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddTrailLimitPercentDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.TRL_LMT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        order.setTrailPercent(0.001f);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddNotTransmitMarketOrderDoesNotFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSide(SideEnum.BUY);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(false);
        order.setTrailPercent(0.001f);
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, order.getStatus());
        Assertions.assertFalse(order.isFinalized());
        Assertions.assertEquals(0, order.getTrades().size());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testAddSingleTransmitOCAMarketOrderFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order order = new Order();
        order.setOrderType(OrderTypeEnum.MKT);
        order.setSide(SideEnum.SELL);
        order.setTimeInForce(TimeInForceEnum.GTC);
        order.setQuantity(4f);
        order.setUser(new User());
        order.setSymbol(symbol);
        order.setTransmit(true);
        order.setOCAGroup("TestOCAGroup");
        Field orderID = order.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(order, 1);

        backtesterSharedService.addOrder(key, order);

        Assertions.assertEquals(OrderStatusEnum.FILLED, order.getStatus());
        Assertions.assertTrue(order.isFinalized());
        Assertions.assertEquals(1, order.getTrades().size());
        Assertions.assertEquals(3, order.getTrades().getFirst().getFillPrice());
        Mockito.verify(orderDAO, Mockito.atLeast(1)).update(Mockito.any(Order.class));
    }

    @Test
    public void testFillFirstMarketOrderInOCAGroupWhenSecondLastLimitOrderIsAdded() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order filledOrder = new Order();
        filledOrder.setOrderType(OrderTypeEnum.MKT);
        filledOrder.setSide(SideEnum.SELL);
        filledOrder.setTimeInForce(TimeInForceEnum.GTC);
        filledOrder.setQuantity(4f);
        filledOrder.setUser(new User());
        filledOrder.setSymbol(symbol);
        filledOrder.setTransmit(false);
        filledOrder.setOCAGroup("TestOCAGroup");
        Field orderID = filledOrder.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(filledOrder, 1);

        backtesterSharedService.addOrder(key, filledOrder);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, filledOrder.getStatus());
        Assertions.assertFalse(filledOrder.isFinalized());

        Order cancelledOrder = new Order();
        cancelledOrder.setPrice(1f);
        cancelledOrder.setOrderType(OrderTypeEnum.LMT);
        cancelledOrder.setSide(SideEnum.BUY);
        cancelledOrder.setTimeInForce(TimeInForceEnum.GTC);
        cancelledOrder.setQuantity(4f);
        cancelledOrder.setUser(new User());
        cancelledOrder.setSymbol(symbol);
        cancelledOrder.setTransmit(true);
        cancelledOrder.setOCAGroup("TestOCAGroup");
        Field cancelledOrderID = cancelledOrder.getClass().getDeclaredField("orderID");
        cancelledOrderID.setAccessible(true);
        cancelledOrderID.set(cancelledOrder, 2);

        backtesterSharedService.addOrder(key, cancelledOrder);

        Assertions.assertEquals(OrderStatusEnum.FILLED, filledOrder.getStatus());
        Assertions.assertTrue(filledOrder.isFinalized());
        Assertions.assertEquals(1, filledOrder.getTrades().size());
        Assertions.assertEquals(3, filledOrder.getTrades().getFirst().getFillPrice());

        Assertions.assertEquals(OrderStatusEnum.CANCELLED, cancelledOrder.getStatus());
        Assertions.assertTrue(cancelledOrder.isFinalized());
        Assertions.assertEquals(0, cancelledOrder.getTrades().size());

        Mockito.verify(orderDAO, Mockito.atLeast(2)).update(Mockito.any(Order.class));
    }

    @Test
    public void testFillSecondMarketOrderInOCAGroupWhenAdded() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order filledOrder = new Order();
        filledOrder.setOrderType(OrderTypeEnum.MKT);
        filledOrder.setSide(SideEnum.SELL);
        filledOrder.setTimeInForce(TimeInForceEnum.GTC);
        filledOrder.setQuantity(4f);
        filledOrder.setUser(new User());
        filledOrder.setSymbol(symbol);
        filledOrder.setTransmit(false);
        filledOrder.setOCAGroup("TestOCAGroup");
        Field orderID = filledOrder.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(filledOrder, 1);

        backtesterSharedService.addOrder(key, filledOrder);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, filledOrder.getStatus());
        Assertions.assertFalse(filledOrder.isFinalized());

        Order cancelledOrder = new Order();
        cancelledOrder.setOrderType(OrderTypeEnum.MKT);
        cancelledOrder.setSide(SideEnum.BUY);
        cancelledOrder.setTimeInForce(TimeInForceEnum.GTC);
        cancelledOrder.setQuantity(4f);
        cancelledOrder.setUser(new User());
        cancelledOrder.setSymbol(symbol);
        cancelledOrder.setTransmit(true);
        cancelledOrder.setOCAGroup("TestOCAGroup");
        Field cancelledOrderID = cancelledOrder.getClass().getDeclaredField("orderID");
        cancelledOrderID.setAccessible(true);
        cancelledOrderID.set(cancelledOrder, 2);

        backtesterSharedService.addOrder(key, cancelledOrder);

        Assertions.assertEquals(OrderStatusEnum.FILLED, cancelledOrder.getStatus());
        Assertions.assertTrue(cancelledOrder.isFinalized());
        Assertions.assertEquals(1, cancelledOrder.getTrades().size());
        Assertions.assertEquals(3, cancelledOrder.getTrades().getFirst().getFillPrice());

        Assertions.assertEquals(OrderStatusEnum.CANCELLED, filledOrder.getStatus());
        Assertions.assertTrue(filledOrder.isFinalized());
        Assertions.assertEquals(0, filledOrder.getTrades().size());

        Mockito.verify(orderDAO, Mockito.atLeast(2)).update(Mockito.any(Order.class));
    }

    @Test
    public void testFillOneOCAGroupDoesNotAffectAnother() throws Exception{
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order otherOrder = new Order();
        otherOrder.setOrderType(OrderTypeEnum.MKT);
        otherOrder.setSide(SideEnum.BUY);
        otherOrder.setTimeInForce(TimeInForceEnum.GTC);
        otherOrder.setQuantity(4f);
        otherOrder.setUser(new User());
        otherOrder.setSymbol(symbol);
        otherOrder.setTransmit(false);
        otherOrder.setOCAGroup("OtherTestOCAGroup");
        Field otherOrderID = otherOrder.getClass().getDeclaredField("orderID");
        otherOrderID.setAccessible(true);
        otherOrderID.set(otherOrder, 2);

        backtesterSharedService.addOrder(key, otherOrder);

        Order filledOrder = new Order();
        filledOrder.setOrderType(OrderTypeEnum.MKT);
        filledOrder.setSide(SideEnum.SELL);
        filledOrder.setTimeInForce(TimeInForceEnum.GTC);
        filledOrder.setQuantity(4f);
        filledOrder.setUser(new User());
        filledOrder.setSymbol(symbol);
        filledOrder.setTransmit(true);
        filledOrder.setOCAGroup("TestOCAGroup");
        Field orderID = filledOrder.getClass().getDeclaredField("orderID");
        orderID.setAccessible(true);
        orderID.set(filledOrder, 1);

        backtesterSharedService.addOrder(key, filledOrder);

        Assertions.assertEquals(OrderStatusEnum.FILLED, filledOrder.getStatus());
        Assertions.assertTrue(filledOrder.isFinalized());
        Assertions.assertEquals(1, filledOrder.getTrades().size());
        Assertions.assertEquals(3, filledOrder.getTrades().getFirst().getFillPrice());

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, otherOrder.getStatus());
        Assertions.assertFalse(otherOrder.isFinalized());
        Assertions.assertEquals(0, otherOrder.getTrades().size());

        Mockito.verify(orderDAO, Mockito.atLeast(2)).update(Mockito.any(Order.class));
    }

    @Test
    public void testParentLimitChildMarketBothBlocked() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order parentOrder = new Order();
        parentOrder.setPrice(0f);
        parentOrder.setOrderType(OrderTypeEnum.LMT);
        parentOrder.setSide(SideEnum.BUY);
        parentOrder.setTimeInForce(TimeInForceEnum.GTC);
        parentOrder.setQuantity(4f);
        parentOrder.setUser(new User());
        parentOrder.setSymbol(symbol);
        parentOrder.setTransmit(false);
        Field parentOrderID = parentOrder.getClass().getDeclaredField("orderID");
        parentOrderID.setAccessible(true);
        parentOrderID.set(parentOrder, 1);

        backtesterSharedService.addOrder(key, parentOrder);

        Order childOrder = new Order();
        childOrder.setOrderType(OrderTypeEnum.MKT);
        childOrder.setSide(SideEnum.BUY);
        childOrder.setTimeInForce(TimeInForceEnum.GTC);
        childOrder.setQuantity(4f);
        childOrder.setUser(new User());
        childOrder.setSymbol(symbol);
        childOrder.setTransmit(true);
        childOrder.setParentOrder(parentOrder);
        Field childOrderID = childOrder.getClass().getDeclaredField("orderID");
        childOrderID.setAccessible(true);
        childOrderID.set(childOrder, 2);

        backtesterSharedService.addOrder(key, childOrder);

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, parentOrder.getStatus());
        Assertions.assertFalse(parentOrder.isFinalized());
        Assertions.assertEquals(0, parentOrder.getTrades().size());

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, childOrder.getStatus());
        Assertions.assertFalse(childOrder.isFinalized());
        Assertions.assertEquals(0, childOrder.getTrades().size());
    }

    @Test
    public void testParentMarketChildLimitDoesNotFillChild() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order parentOrder = new Order();
        parentOrder.setOrderType(OrderTypeEnum.MKT);
        parentOrder.setSide(SideEnum.BUY);
        parentOrder.setTimeInForce(TimeInForceEnum.GTC);
        parentOrder.setQuantity(4f);
        parentOrder.setUser(new User());
        parentOrder.setSymbol(symbol);
        parentOrder.setTransmit(false);
        Field parentOrderID = parentOrder.getClass().getDeclaredField("orderID");
        parentOrderID.setAccessible(true);
        parentOrderID.set(parentOrder, 1);

        backtesterSharedService.addOrder(key, parentOrder);

        Order childOrder = new Order();
        childOrder.setPrice(0f);
        childOrder.setOrderType(OrderTypeEnum.LMT);
        childOrder.setSide(SideEnum.BUY);
        childOrder.setTimeInForce(TimeInForceEnum.GTC);
        childOrder.setQuantity(4f);
        childOrder.setUser(new User());
        childOrder.setSymbol(symbol);
        childOrder.setTransmit(true);
        childOrder.setParentOrder(parentOrder);
        Field childOrderID = childOrder.getClass().getDeclaredField("orderID");
        childOrderID.setAccessible(true);
        childOrderID.set(childOrder, 2);

        backtesterSharedService.addOrder(key, childOrder);

        Assertions.assertEquals(OrderStatusEnum.FILLED, parentOrder.getStatus());
        Assertions.assertTrue(parentOrder.isFinalized());
        Assertions.assertEquals(1, parentOrder.getTrades().size());

        Assertions.assertEquals(OrderStatusEnum.SUBMITTED, childOrder.getStatus());
        Assertions.assertFalse(childOrder.isFinalized());
        Assertions.assertEquals(0, childOrder.getTrades().size());
    }

    @Test
    public void testParentAndChildMarketBothFill() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order parentOrder = new Order();
        parentOrder.setOrderType(OrderTypeEnum.MKT);
        parentOrder.setSide(SideEnum.BUY);
        parentOrder.setTimeInForce(TimeInForceEnum.GTC);
        parentOrder.setQuantity(4f);
        parentOrder.setUser(new User());
        parentOrder.setSymbol(symbol);
        parentOrder.setTransmit(false);
        Field parentOrderID = parentOrder.getClass().getDeclaredField("orderID");
        parentOrderID.setAccessible(true);
        parentOrderID.set(parentOrder, 1);

        backtesterSharedService.addOrder(key, parentOrder);

        Order childOrder = new Order();
        childOrder.setOrderType(OrderTypeEnum.MKT);
        childOrder.setSide(SideEnum.BUY);
        childOrder.setTimeInForce(TimeInForceEnum.GTC);
        childOrder.setQuantity(4f);
        childOrder.setUser(new User());
        childOrder.setSymbol(symbol);
        childOrder.setTransmit(true);
        childOrder.setParentOrder(parentOrder);
        Field childOrderID = childOrder.getClass().getDeclaredField("orderID");
        childOrderID.setAccessible(true);
        childOrderID.set(childOrder, 2);

        backtesterSharedService.addOrder(key, childOrder);

        Assertions.assertEquals(OrderStatusEnum.FILLED, parentOrder.getStatus());
        Assertions.assertTrue(parentOrder.isFinalized());
        Assertions.assertEquals(1, parentOrder.getTrades().size());

        Assertions.assertEquals(OrderStatusEnum.FILLED, childOrder.getStatus());
        Assertions.assertTrue(childOrder.isFinalized());
        Assertions.assertEquals(1, childOrder.getTrades().size());
    }

    @Test
    public void testParentFillsBeforeChildMarketChildStillFills() throws Exception {
        BacktesterDataFeedKey key = BacktesterDataFeedKey.createKeyForSymbol(1);
        Symbol symbol = loadDataAndCreateSymbol(1);

        Order parentOrder = new Order();
        parentOrder.setOrderType(OrderTypeEnum.MKT);
        parentOrder.setSide(SideEnum.BUY);
        parentOrder.setTimeInForce(TimeInForceEnum.GTC);
        parentOrder.setQuantity(4f);
        parentOrder.setUser(new User());
        parentOrder.setSymbol(symbol);
        parentOrder.setTransmit(true);
        Field parentOrderID = parentOrder.getClass().getDeclaredField("orderID");
        parentOrderID.setAccessible(true);
        parentOrderID.set(parentOrder, 1);

        backtesterSharedService.addOrder(key, parentOrder);

        Order childOrder = new Order();
        childOrder.setOrderType(OrderTypeEnum.MKT);
        childOrder.setSide(SideEnum.BUY);
        childOrder.setTimeInForce(TimeInForceEnum.GTC);
        childOrder.setQuantity(4f);
        childOrder.setUser(new User());
        childOrder.setSymbol(symbol);
        childOrder.setTransmit(true);
        childOrder.setParentOrder(parentOrder);
        Field childOrderID = childOrder.getClass().getDeclaredField("orderID");
        childOrderID.setAccessible(true);
        childOrderID.set(childOrder, 2);

        backtesterSharedService.addOrder(key, childOrder);

        Assertions.assertEquals(OrderStatusEnum.FILLED, parentOrder.getStatus());
        Assertions.assertTrue(parentOrder.isFinalized());
        Assertions.assertEquals(1, parentOrder.getTrades().size());

        Assertions.assertEquals(OrderStatusEnum.FILLED, childOrder.getStatus());
        Assertions.assertTrue(childOrder.isFinalized());
        Assertions.assertEquals(1, childOrder.getTrades().size());
    }

    // TODO add cancellation tests
    // TODO add update data feed tests

}
