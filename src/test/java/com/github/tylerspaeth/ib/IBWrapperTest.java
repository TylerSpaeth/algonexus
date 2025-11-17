package com.github.tylerspaeth.ib;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IBWrapperTest {

    private IBConnection ibConnectionMock;
    private IBWrapper wrapper;

    @BeforeEach
    void setup() throws Exception {
        // Create a real IBWrapper
        wrapper = new IBWrapper();

        // Mock the internal IBConnection
        ibConnectionMock = mock(IBConnection.class);

        // Use reflection to replace the private field in IBWrapper
        var field = IBWrapper.class.getDeclaredField("ibConnection");
        field.setAccessible(true);
        field.set(wrapper, ibConnectionMock);
    }

    @Test
    void connect_callsIBConnectionConnect() {
        wrapper.connect();
        verify(ibConnectionMock).connect();
    }

    @Test
    void disconnect_callsIBConnectionDisconnect() {
        wrapper.disconnect();
        verify(ibConnectionMock).disconnect();
    }

    @Test
    void onNextValidId_countsDownLatch() {
        CountDownLatch latch = new CountDownLatch(1);
        when(ibConnectionMock.getHandshakeLatch()).thenReturn(latch);

        wrapper.onNextValidId(42);

        assertEquals(0, latch.getCount(), "Latch should be counted down after onNextValidId");
    }

    @Test
    void onDisconnect_reschedulesConnectionIfNotManual() {
        AtomicBoolean manualDisconnect = new AtomicBoolean(false);
        when(ibConnectionMock.getManualDisconnect()).thenReturn(manualDisconnect);

        wrapper.onDisconnect();

        verify(ibConnectionMock).scheduleConnection(IBConnection.RECONNECT_DELAY_MS);
    }

    @Test
    void onDisconnect_doesNotRescheduleIfManualDisconnect() {
        AtomicBoolean manualDisconnect = new AtomicBoolean(true);
        when(ibConnectionMock.getManualDisconnect()).thenReturn(manualDisconnect);

        wrapper.onDisconnect();

        verify(ibConnectionMock, never()).scheduleConnection(anyInt());
    }

    @Test
    void connectionClosed_triggersOnDisconnect() {
        AtomicBoolean manualDisconnect = new AtomicBoolean(false);
        when(ibConnectionMock.getManualDisconnect()).thenReturn(manualDisconnect);

        wrapper.connectionClosed();

        verify(ibConnectionMock).scheduleConnection(IBConnection.RECONNECT_DELAY_MS);
    }

    @Test
    void connectAck_triggersOnConnect() {
        wrapper.connectAck();
        // We can't directly verify logger output, but this ensures no exceptions thrown
    }
}
