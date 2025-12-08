package com.github.tylerspaeth.ib;

import com.github.tylerspaeth.broker.ib.response.*;
import com.github.tylerspaeth.broker.ib.IBConnection;
import com.github.tylerspaeth.broker.ib.IBRequestRepository;
import com.github.tylerspaeth.broker.ib.IBWrapper;
import com.github.tylerspaeth.common.MultiReaderQueue;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.Execution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IBWrapperTest {

    IBConnection ibConnection;
    IBWrapper wrapper;

    @BeforeEach
    void setup() {
        ibConnection = new IBConnection();
        wrapper = new IBWrapper(ibConnection);
    }

    // -------------------------
    // Order Handling Tests
    // -------------------------
    @Test
    void orderStatus_updatesOrderStateCorrectly() {
        int orderId = 1;
        OrderResponse state = new OrderResponse(1, null, null);
        ibConnection.orderStateMap.put(orderId, state);

        wrapper.orderStatus(orderId, "Filled", Decimal.get(5), Decimal.get(0), 123.45, 0L, 0, 123.45, 0, "", 0);

        assertEquals(5, state.cumulativeFilled);
    }

    @Test
    void execDetails_addsExecutionToOrderState() {
        int orderId = 2;
        OrderResponse state = spy(new OrderResponse(1, null, null));
        ibConnection.orderStateMap.put(orderId, state);

        Contract contract = new Contract();
        Execution exec = mock(Execution.class);
        when(exec.orderId()).thenReturn(orderId);

        wrapper.execDetails(0, contract, exec);

        verify(state).addExecution(exec);
    }

    @Test
    void execDetailsEnd_setsExecDetailsEnded() {
        int reqId = 3;
        OrderResponse state = new OrderResponse(1, null, null);
        ibConnection.orderStateMap.put(reqId, state);

        wrapper.execDetailsEnd(reqId);

        assertTrue(state.execDetailsEnded);
    }

    // -------------------------
    // Datafeed Handling Tests
    // -------------------------
    @Test
    void realtimeBar_writesToQueue() {
        IBConnection ibConnection = new IBConnection();

        // Subscribe a reader
        MultiReaderQueue<RealtimeBar> queue = new MultiReaderQueue<>();
        queue.subscribe();

        // Put the queue in the connection's map as it would be in real usage
        int reqId = 42;
        ibConnection.datafeedReqIdMap.put(reqId, queue);

        IBWrapper wrapper = new IBWrapper(ibConnection);

        long epochSeconds = 1700000000L;
        wrapper.realtimeBar(reqId, epochSeconds, 100.0, 110.0, 90.0, 105.0,
                Decimal.get(1000), Decimal.get(102.5), 10);

        // Read from the queue
        RealtimeBar realtimeBar = queue.read();

        assertNotNull(realtimeBar);
        assertEquals(100.0, realtimeBar.open());
        assertEquals(110.0, realtimeBar.high());
        assertEquals(90.0, realtimeBar.low());
        assertEquals(105.0, realtimeBar.close());
        assertEquals(Decimal.get(1000), realtimeBar.volume());
        assertEquals(epochSeconds, realtimeBar.date());
    }


    // -------------------------
    // Request Repository Tests
    // -------------------------
    @Test
    void contractDetails_addsContractDetailsToRepository() {
        int reqId = 5;
        ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        List<ContractDetails> existing = new ArrayList<>();
        ibConnection.ibRequestRepository.setFutureValue(String.valueOf(reqId), existing);

        wrapper.contractDetails(reqId, new com.ib.client.ContractDetails());

        List<ContractDetails> result = ibConnection.ibRequestRepository.getFutureValue(String.valueOf(reqId));
        assertEquals(1, result.size());
    }

    @Test
    void position_addsPositionToRepository() {

        ibConnection.ibRequestRepository.registerPendingRequest(IBRequestRepository.POSITION_REQ_MAP_KEY);

        String accountId = "ACC1";
        Contract contract = new Contract();
        wrapper.position(accountId, contract, Decimal.get(5), 100);

        List<Position> response = ibConnection.ibRequestRepository.getFutureValue(IBRequestRepository.POSITION_REQ_MAP_KEY);
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(5, response.getFirst().position().value().intValue());
    }

    @Test
    void accountSummary_addsSummaryToRepository() {
        int reqId = 7;
        ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        wrapper.accountSummary(reqId, "ACC", "Tag", "Value", "USD");

        List<AccountSummary> response = ibConnection.ibRequestRepository.getFutureValue(String.valueOf(reqId));
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("ACC", response.getFirst().accountID());
        assertEquals("Tag", response.getFirst().tag());
    }

    @Test
    void pnl_setsAccountPnLInRepository() throws Exception{
        int reqId = 8;
        var future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        wrapper.pnl(reqId, 1.0, 2.0, 3.0);

        AccountPnL response = (AccountPnL) future.get(5, TimeUnit.SECONDS);
        assertEquals(1.0, response.dailyPnL());
        assertEquals(2.0, response.unrealizedPnL());
        assertEquals(3.0, response.realizedPnL());
    }

    // -------------------------
    // Wrapper-to-Connection Callbacks
    // -------------------------
    @Test
    void nextValidId_callsOnNextValidId() {
        AtomicInteger nextValid = ibConnection.nextValidId;
        wrapper.nextValidId(42);
        assertEquals(42, nextValid.get());
    }

    @Test
    void connectAck_callsOnConnectAck() {
        IBConnection spyConn = spy(ibConnection);
        IBWrapper w = new IBWrapper(spyConn);
        w.connectAck();
        verify(spyConn).onConnectAck();
    }

    @Test
    void connectionClosed_callsOnConnectionClosed() {
        IBConnection spyConn = spy(ibConnection);
        IBWrapper w = new IBWrapper(spyConn);
        w.connectionClosed();
        verify(spyConn).onConnectionClosed();
    }

    @Test
    void readUnsubscribed_returnsNull() {
        MultiReaderQueue<String> queue = new MultiReaderQueue<>();
        String result = queue.read();
        assertNull(result, "Reading without subscription should return null");
    }

    @Test
    void dumpUnsubscribed_returnsNull() {
        MultiReaderQueue<String> queue = new MultiReaderQueue<>();
        assertEquals(new ArrayList<>(), queue.dump());
    }

    @Test
    void getFutureValueForUnknownKey_returnsNull() {
        assertNull(ibConnection.ibRequestRepository.getFutureValue("nonexistent"),
                "Getting value for unknown key should return null");
    }

    @Test
    void setFutureValueForUnknownKey_doesNotThrow() {
        assertDoesNotThrow(() -> ibConnection.ibRequestRepository.setFutureValue("nonexistent", "value"));
    }

    @Test
    void removePendingRequestForUnknownKey_doesNotThrow() {
        assertDoesNotThrow(() -> ibConnection.ibRequestRepository.removePendingRequest("nonexistent"));
    }

    @Test
    void removePendingRequestWithExceptionForUnknownKey_doesNotThrow() {
        assertDoesNotThrow(() -> ibConnection.ibRequestRepository.removePendingRequestWithException("nonexistent",
                new RuntimeException("error")));
    }

    @Test
    void registerPendingRequestTwice_returnsNullSecondTime() {
        String reqId = "REQ1";
        assertNotNull(ibConnection.ibRequestRepository.registerPendingRequest(reqId));
        assertNull(ibConnection.ibRequestRepository.registerPendingRequest(reqId),
                "Registering same request twice should return null");
    }

    @Test
    void orderStatusWithUnknownOrderId_doesNotThrow() {
        assertDoesNotThrow(() -> wrapper.orderStatus(999, "Filled", Decimal.get(1), Decimal.get(0),
                10.0, 0L, 0, 10.0, 0, "", 0));
    }

    @Test
    void execDetailsEndWithUnknownOrderId_doesNotThrow() {
        assertDoesNotThrow(() -> wrapper.execDetailsEnd(12345));
    }

    @Test
    void positionWithoutRegisteringRequest_returnsNullFuture() {
        String accountId = "ACC1";
        Contract contract = new Contract();
        assertDoesNotThrow(() -> wrapper.position(accountId, contract, Decimal.get(5), 100));
        List<Position> response = ibConnection.ibRequestRepository.getFutureValue(IBRequestRepository.POSITION_REQ_MAP_KEY);
        assertNull(response, "If request not registered, future should be null");
    }

    @Test
    void accountSummaryWithoutRegisteringRequest_returnsNullFuture() {
        wrapper.accountSummary(1, "ACC", "Tag", "Value", "USD");
        List<AccountSummary> response = ibConnection.ibRequestRepository.getFutureValue("1");
        assertNull(response, "If request not registered, future should be null");
    }

}
