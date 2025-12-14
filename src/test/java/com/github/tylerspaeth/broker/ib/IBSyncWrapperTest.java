package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.DataFeedKey;
import com.github.tylerspaeth.broker.ib.response.*;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.ib.client.*;
import com.ib.controller.AccountSummaryTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@ExtendWith(MockitoExtension.class)
public class IBSyncWrapperTest {

    private static final long SLEEP_BEFORE_CALLBACK_MS = 100L;

    @Mock
    private EClientSocket client; // Mock client so real tws requests are not made

    private IBConnection connection;
    private IBSyncWrapper wrapper;

    @BeforeEach
    public void setup() {
        connection = new IBConnection();
        connection.client = client;
        wrapper = IBSyncWrapper.getInstanceTest(connection);
    }

    @Test
    public void testAccountSummary() throws Exception {
        int reqId = connection.nextValidId.get();
        // Simulate the IBWrapper callback in another thread
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_BEFORE_CALLBACK_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            connection.getWrapper().accountSummary(
                    reqId, "ACC123", "NetLiquidation", "1000", "USD"
            );
            connection.getWrapper().accountSummaryEnd(reqId);
        }, "Mock-Callback").start();

        List<AccountSummary> summaries = wrapper.getAccountSummary("ALL", List.of(AccountSummaryTag.NetLiquidation));

        Assertions.assertEquals(1, summaries.size());
        Assertions.assertEquals("ACC123", summaries.getFirst().accountID());
    }

    @Test
    public void testGetPositions() throws Exception {
        // Simulate the IBWrapper callback in another thread
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_BEFORE_CALLBACK_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Contract contract = new Contract();
            contract.conid(111);
            connection.getWrapper().position("AccountID", contract, Decimal.ONE, 12.3);
            connection.getWrapper().positionEnd();
        }, "Mock-Callback").start();

        List<Position> positions = wrapper.getPositions();

        Assertions.assertEquals(1, positions.size());
        Assertions.assertEquals(111, positions.getFirst().contract().conid());
        Assertions.assertEquals(Decimal.ONE, positions.getFirst().position());
        Assertions.assertEquals(12.3, positions.getFirst().avgCost());
    }

    @Test
    public void testGetAccountPnL() throws Exception {
        int reqId = connection.nextValidId.get();

        // Simulate the IBWrapper callback in another thread
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_BEFORE_CALLBACK_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            connection.getWrapper().pnl(reqId, 100, 200, 300);
        }, "Mock-Callback").start();

        AccountPnL accountPnL = wrapper.getAccountPnL("TestAccount", "");

        Assertions.assertEquals(100, accountPnL.dailyPnL());
        Assertions.assertEquals(200, accountPnL.unrealizedPnL());
        Assertions.assertEquals(300, accountPnL.realizedPnL());
    }

    @Test
    public void testGetPositionPnl() throws Exception {
        int reqId = connection.nextValidId.get();

        // Simulate the IBWrapper callback in another thread
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_BEFORE_CALLBACK_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            connection.getWrapper().pnlSingle(reqId, Decimal.ONE, 100, 200, 300, 400);
        }, "Mock-Callback").start();

        PositionPnL positionPnL = wrapper.getPositionPnL("TestAccount", "", 1);

        Assertions.assertEquals(Decimal.ONE, positionPnL.position());
        Assertions.assertEquals(100, positionPnL.dailyPnL());
        Assertions.assertEquals(200, positionPnL.unrealizedPnL());
        Assertions.assertEquals(300, positionPnL.realizedPnL());
        Assertions.assertEquals(400, positionPnL.value());
    }

    @Test
    public void testGetMatchingSymbols() throws Exception {
        int reqId = connection.nextValidId.get();

        // Simulate the IBWrapper callback in another thread
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_BEFORE_CALLBACK_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            ContractDescription contractDescription1 = new ContractDescription();
            contractDescription1.contract().conid(1);
            ContractDescription contractDescription2 = new ContractDescription();
            contractDescription2.contract().conid(2);
            connection.getWrapper().symbolSamples(reqId, List.of(contractDescription1, contractDescription2).toArray(new ContractDescription[2]));
        }, "Mock-Callback").start();

        ContractDescription[] contractDescriptions = wrapper.getMatchingSymbols("TestLookupValue");

        Assertions.assertEquals(2, contractDescriptions.length);
        Assertions.assertEquals(1, contractDescriptions[0].contract().conid());
        Assertions.assertEquals(2, contractDescriptions[1].contract().conid());
    }

    @Test
    public void testGetContractDetails() throws Exception {
        int reqId = connection.nextValidId.get();

        // Simulate the IBWrapper callback in another thread
        new Thread(() -> {
            try {
                Thread.sleep(SLEEP_BEFORE_CALLBACK_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            ContractDetails contractDetails1 = new ContractDetails();
            contractDetails1.contract().conid(1);
            ContractDetails contractDetails2 = new ContractDetails();
            contractDetails2.contract().conid(2);

            connection.getWrapper().contractDetails(reqId, contractDetails1);
            connection.getWrapper().contractDetails(reqId, contractDetails2);
            connection.getWrapper().contractDetailsEnd(reqId);
        }, "Mock-Callback").start();

        List<ContractDetails> contractDetails = wrapper.getContractDetails(new Contract());

        Assertions.assertEquals(2, contractDetails.size());
        Assertions.assertEquals(1, contractDetails.getFirst().contract().conid());
        Assertions.assertEquals(2, contractDetails.getLast().contract().conid());
    }

    @Test
    public void testSubscribeToNewFeed() {
        wrapper.subscribeToDataFeed(new DataFeedKey(null, "ticker", "secType", "exchange", "currency"));
        Mockito.verify(client, Mockito.times(1)).reqRealTimeBars(Mockito.anyInt(), Mockito.any(Contract.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
        Assertions.assertEquals(1, connection.datafeeds.size());
    }

    @Test
    public void testSubscribeToSameFeedFromDifferentThreads() throws InterruptedException {

        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");

        wrapper.subscribeToDataFeed(dataFeedKey);
        Mockito.verify(client, Mockito.times(1)).reqRealTimeBars(Mockito.anyInt(), Mockito.any(Contract.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());

        Mockito.clearInvocations(client);

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            wrapper.subscribeToDataFeed(dataFeedKey);
            Mockito.verify(client, Mockito.times(0)).reqRealTimeBars(Mockito.anyInt(), Mockito.any(Contract.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
            latch.countDown();
        }, "OtherThread").start();

        latch.await();

        Assertions.assertEquals(1, connection.datafeeds.size());
    }

    @Test
    public void testSubscribeToDifferentFeedsFromSingleThread() {
        wrapper.subscribeToDataFeed(new DataFeedKey(null, "ticker", "secType", "exchange", "currency"));
        wrapper.subscribeToDataFeed(new DataFeedKey(null, "otherTicker", "secType", "exchange", "currency"));
        Mockito.verify(client, Mockito.times(2)).reqRealTimeBars(Mockito.anyInt(), Mockito.any(Contract.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
        Assertions.assertEquals(2, connection.datafeeds.size());
    }

    @Test
    public void testSubscribeAgainAfterUnsubscribing() {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);
        wrapper.unsubscribeFromDataFeed(dataFeedKey);
        wrapper.subscribeToDataFeed(dataFeedKey);

        Mockito.verify(client, Mockito.times(2)).reqRealTimeBars(Mockito.anyInt(), Mockito.any(Contract.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
        Mockito.verify(client, Mockito.times(1)).cancelRealTimeBars(Mockito.anyInt());
        Assertions.assertEquals(1, connection.datafeeds.size());
    }

    @Test
    public void testReadFromUnsubscribedDatafeed() {
        Assertions.assertTrue(wrapper.readFromDataFeed(new DataFeedKey(null, "ticker", "secType", "exchange", "currency"), 1, IntervalUnitEnum.SECOND   ).isEmpty());
    }

    @Test
    public void testReadFromSameFeedFromDifferentThreads() {

        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");

        wrapper.subscribeToDataFeed(dataFeedKey);

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            wrapper.subscribeToDataFeed(dataFeedKey);
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<RealtimeBar> realtimeBars = wrapper.readFromDataFeed(dataFeedKey, 5, IntervalUnitEnum.SECOND);
            Assertions.assertEquals(1, realtimeBars.size());
        }, "OtherThread").start();

        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 0, 1, 2, 3, 4, Decimal.ONE_HUNDRED, null, -1);

        latch.countDown();

        List<RealtimeBar> realtimeBars = wrapper.readFromDataFeed(dataFeedKey, 5, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, realtimeBars.size());
        Assertions.assertEquals(0, realtimeBars.getFirst().date());
        Assertions.assertEquals(1, realtimeBars.getFirst().open());
        Assertions.assertEquals(2, realtimeBars.getFirst().high());
        Assertions.assertEquals(3, realtimeBars.getFirst().low());
        Assertions.assertEquals(4, realtimeBars.getFirst().close());
        Assertions.assertEquals(Decimal.ONE_HUNDRED, realtimeBars.getFirst().volume());
    }

    @Test
    public void testReadAtSubFiveSecondGranularity() {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);
        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 0, 1, 2, 3, 4, Decimal.ONE_HUNDRED, null, -1);
        Assertions.assertTrue(wrapper.readFromDataFeed(dataFeedKey, 1, IntervalUnitEnum.SECOND).isEmpty());
    }

    @Test
    public void testReadAtSevenSecondGranularity() {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);
        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 0, 1, 2, 3, 4, Decimal.ONE_HUNDRED, null, -1);
        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 5, 1, 2, 3, 4, Decimal.ONE_HUNDRED, null, -1);
        Assertions.assertTrue(wrapper.readFromDataFeed(dataFeedKey, 7, IntervalUnitEnum.SECOND).isEmpty());
    }

    @Test
    public void testReadFromDataFeedCondenseBars() {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);
        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 0, 2, 2, 1, 1.5, Decimal.ONE_HUNDRED, null, -1);
        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 5, 1.5, 5, 2, 4, Decimal.ONE_HUNDRED, null, -1);
        List<RealtimeBar> realtimeBars = wrapper.readFromDataFeed(dataFeedKey, 10, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, realtimeBars.size());
        Assertions.assertEquals(0, realtimeBars.getFirst().date());
        Assertions.assertEquals(2, realtimeBars.getFirst().open());
        Assertions.assertEquals(5, realtimeBars.getFirst().high());
        Assertions.assertEquals(1, realtimeBars.getFirst().low());
        Assertions.assertEquals(4, realtimeBars.getFirst().close());
        Assertions.assertEquals(Decimal.get(200), realtimeBars.getFirst().volume());
        Assertions.assertEquals(1, connection.datafeeds.size());
    }

    @Test
    public void testUnsubscribeCancelsIBSubscription() {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);
        wrapper.unsubscribeFromDataFeed(dataFeedKey);
        Mockito.verify(client, Mockito.times(1)).cancelRealTimeBars(Mockito.anyInt());
        Assertions.assertTrue(connection.datafeeds.isEmpty());
    }

    @Test
    public void testUnsubscribeDoesNotCancelIfNotSubscribed() {
        wrapper.unsubscribeFromDataFeed(new DataFeedKey(null, "ticker", "secType", "exchange", "currency"));
        Mockito.verify(client, Mockito.times(0)).cancelRealTimeBars(Mockito.anyInt());
    }

    @Test
    public void testUnsubscribeDoesNotCancelIfThereIsAnotherSubscriber() throws InterruptedException {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            wrapper.subscribeToDataFeed(dataFeedKey);
            latch.countDown();
        }, "OtherThread").start();

        latch.await();

        wrapper.unsubscribeFromDataFeed(dataFeedKey);
        Mockito.verify(client, Mockito.times(0)).cancelRealTimeBars(Mockito.anyInt());
    }

    @Test
    public void testReadAfterUnsubscribe() {
        DataFeedKey dataFeedKey = new DataFeedKey(null, "ticker", "secType", "exchange", "currency");
        wrapper.subscribeToDataFeed(dataFeedKey);
        connection.getWrapper().realtimeBar(dataFeedKey.getReqId(), 0, 2, 2, 1, 1.5, Decimal.ONE_HUNDRED, null, -1);
        wrapper.unsubscribeFromDataFeed(dataFeedKey);
        List<RealtimeBar> realtimeBars = wrapper.readFromDataFeed(dataFeedKey, 5, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(0, realtimeBars.size());
    }

}