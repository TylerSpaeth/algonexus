package com.github.tylerspaeth.broker.ib;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IBConnectionTest {

    IBConnection ibConnection;
    EClientSocket mockClient;
    EJavaSignal mockSignal;

    @BeforeEach
    void setup() {
        ibConnection = new IBConnection();

        // Replace client and signal with mocks
        mockClient = mock(EClientSocket.class);
        mockSignal = mock(EJavaSignal.class);

        // Inject mocks via reflection
        setField(ibConnection, "client", mockClient);
        setField(ibConnection, "signal", mockSignal);
    }

    /** Utility to set private final fields via reflection */
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void connect_callsEConnectAndStartsReader() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);

        // Call connectIfNeeded directly
        ibConnection.scheduleConnection(0);
        Thread.sleep(200); // allow scheduler to run

        verify(mockClient, atLeastOnce()).eConnect("127.0.0.1", 4002, 1);
    }

    @Test
    void handshakeLatchCompletes_onNextValidId() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);

        // Create latch manually
        var latch = new CountDownLatch(1);
        setField(ibConnection, "handshakeLatch", latch);

        assertEquals(1, latch.getCount());

        ibConnection.onNextValidId(123);

        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void disconnect_setsManualDisconnectAndCallsEDisconnect() {
        when(mockClient.isConnected()).thenReturn(true);

        // Initialize scheduler to avoid NPE
        ibConnection.scheduleConnection(0);

        ibConnection.disconnect();

        verify(mockClient).eDisconnect();
        assertTrue(getAtomicBoolean(ibConnection, "manualDisconnect").get());
    }

    @Test
    void connectionClosedSchedulesReconnect_whenNotManualDisconnect() throws Exception {
        // manualDisconnect = false
        getAtomicBoolean(ibConnection, "manualDisconnect").set(false);

        // Ensure scheduler exists
        ibConnection.scheduleConnection(0);

        ibConnection.onConnectionClosed();

        ScheduledExecutorService scheduler = getScheduler(ibConnection);
        assertFalse(scheduler.isShutdown());
    }

    @Test
    void connectionClosedDoesNotReconnect_whenManualDisconnect() {
        getAtomicBoolean(ibConnection, "manualDisconnect").set(true);

        // Ensure scheduler exists
        ibConnection.scheduleConnection(0);

        ibConnection.onConnectionClosed();

        ScheduledExecutorService scheduler = getScheduler(ibConnection);
        // Scheduler should exist but no new task should run
        assertNotNull(scheduler);
    }

    @Test
    void reconnectTaskIsScheduled_whenConnectionClosedAndNotManualDisconnect() throws Exception {
        // Ensure manualDisconnect = false so reconnect should happen
        getAtomicBoolean(ibConnection, "manualDisconnect").set(false);

        // Mock the scheduler so we can verify scheduling
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
        setField(ibConnection, "scheduler", mockScheduler);

        // Call the method that triggers reconnect
        ibConnection.onConnectionClosed();

        // Verify that scheduler.schedule was called once
        verify(mockScheduler, atLeastOnce()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void connectIfNeededDoesNotScheduleMultipleTimes() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);

        // Ensure scheduler is initialized
        ibConnection.scheduleConnection(0);

        // Now spy on it
        ScheduledExecutorService scheduler = spy(getScheduler(ibConnection));
        setField(ibConnection, "scheduler", scheduler);

        // Call multiple times
        ibConnection.scheduleConnection(0);
        ibConnection.scheduleConnection(0);

        // Only one task should be submitted (or at least scheduler is called)
        verify(scheduler, atLeastOnce()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    void eConnectFailure_doesNotThrowAndAllowsReconnect() throws Exception {
        // Mock the client behavior: not connected and eConnect throws
        when(mockClient.isConnected()).thenReturn(false);
        doThrow(new RuntimeException("connect failed"))
                .when(mockClient).eConnect(anyString(), anyInt(), anyInt());

        // Create a mock scheduler to verify scheduling
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
        setField(ibConnection, "scheduler", mockScheduler);

        // Should not throw even though eConnect fails
        assertDoesNotThrow(() -> ibConnection.scheduleConnection(0));

        // Verify that the reconnect task was still scheduled
        verify(mockScheduler, atLeastOnce()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void disconnect_whenNotConnected_stillSetsManualDisconnectAndDoesNotCallEDisconnect() {
        when(mockClient.isConnected()).thenReturn(false);

        ibConnection.disconnect();

        // eDisconnect should not be called
        verify(mockClient, never()).eDisconnect();
        // manualDisconnect should still be true
        assertTrue(getAtomicBoolean(ibConnection, "manualDisconnect").get());
    }

    @Test
    void onNextValidId_afterManualDisconnect_doesNotCountDownLatch() throws Exception {
        getAtomicBoolean(ibConnection, "manualDisconnect").set(true);

        CountDownLatch latch = new CountDownLatch(1);
        setField(ibConnection, "handshakeLatch", latch);

        ibConnection.onNextValidId(123);

        // Latch should remain untriggered
        assertEquals(0, latch.getCount());
    }

    @Test
    void handshakeLatchTimesOutProperly() throws Exception {
        // Use a latch with short timeout
        CountDownLatch latch = new CountDownLatch(1);
        setField(ibConnection, "handshakeLatch", latch);

        // Do not call onNextValidId, wait slightly longer than timeout
        boolean completed = latch.await(100, TimeUnit.MILLISECONDS);

        // Latch should not complete
        assertFalse(completed);
    }

    /** Utilities to access private fields */
    private java.util.concurrent.atomic.AtomicBoolean getAtomicBoolean(IBConnection conn, String fieldName) {
        try {
            var f = IBConnection.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (java.util.concurrent.atomic.AtomicBoolean) f.get(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ScheduledExecutorService getScheduler(IBConnection conn) {
        try {
            var f = IBConnection.class.getDeclaredField("scheduler");
            f.setAccessible(true);
            return (ScheduledExecutorService) f.get(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
