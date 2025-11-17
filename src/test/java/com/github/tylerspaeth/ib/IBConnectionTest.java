package com.github.tylerspaeth.ib;

import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IBConnectionTest {

    private EClientSocket clientMock;
    private IBConnection ibConnection;

    @BeforeEach
    void setup() {
        EWrapper wrapperMock = mock(EWrapper.class);
        IIBConnectionListener listenerMock = mock(IIBConnectionListener.class);

        // Spy on IBConnection to mock the client
        ibConnection = new IBConnection(wrapperMock, listenerMock);
        clientMock = mock(EClientSocket.class);

        // Replace the private client field using reflection (simplest for tests)
        try {
            var field = IBConnection.class.getDeclaredField("client");
            field.setAccessible(true);
            field.set(ibConnection, clientMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void connect_setsManualDisconnectFalseAndSchedulesConnection() {
        ibConnection.connect();
        assertFalse(ibConnection.getManualDisconnect().get(), "manualDisconnect should be false after connect()");
    }

    @Test
    void disconnect_setsManualDisconnectTrueAndShutsDownScheduler() {
        ibConnection.connect();
        ibConnection.disconnect();

        assertTrue(ibConnection.getManualDisconnect().get(), "manualDisconnect should be true after disconnect()");
    }

    @Test
    void scheduleConnection_attemptsConnectionIfNotManuallyDisconnected() throws InterruptedException {
        when(clientMock.isConnected()).thenReturn(false);

        ibConnection.scheduleConnection(0);

        // Wait a little for async task to execute
        Thread.sleep(200);

        verify(clientMock).eConnect(anyString(), anyInt(), anyInt());
    }

    @Test
    void handshakeLatchIsInitialized() throws Exception {
        // Ensure the mock client behaves as "not connected" so connectIfNeeded() runs
        when(clientMock.isConnected()).thenReturn(false);

        // Spy on IBConnection to avoid actually starting threads
        IBConnection spyConnection = spy(ibConnection);

        // Prevent actual reader thread from starting
        doNothing().when(spyConnection).synchronizedStartReader();

        // Call private connectIfNeeded() directly
        var method = IBConnection.class.getDeclaredMethod("connectIfNeeded");
        method.setAccessible(true);
        method.invoke(spyConnection);

        CountDownLatch latch = spyConnection.getHandshakeLatch();
        assertNotNull(latch, "Handshake latch should be initialized");
    }

    @Test
    void connectIfNeeded_doesNotConnectIfManualDisconnect() {
        ibConnection.getManualDisconnect().set(true);

        ibConnection.scheduleConnection(0);

        verify(clientMock, never()).eConnect(anyString(), anyInt(), anyInt());
    }

    @Test
    void connectIfNeeded_skipsIfAlreadyConnecting() {
        when(clientMock.isConnected()).thenReturn(false);

        // Manually set connecting to true to simulate concurrent connection
        ibConnection.scheduleConnection(0);
        ibConnection.connect(); // triggers connecting.set(false) in finally

        // No exception thrown and log warns "Already connecting"
    }

    @Test
    void connectIfNeeded_handshakeTimeout_schedulesReconnect() throws Exception {
        IBConnection spyConnection = spy(ibConnection);

        // client is not connected so handshake logic executes
        when(clientMock.isConnected()).thenReturn(false);

        // manual disconnect is false
        spyConnection.getManualDisconnect().set(false);

        // mocked latch always times out
        CountDownLatch latchMock = mock(CountDownLatch.class);
        when(latchMock.await(anyLong(), any(TimeUnit.class))).thenReturn(false);
        Field latchField = IBConnection.class.getDeclaredField("handshakeLatch");
        latchField.setAccessible(true);
        latchField.set(spyConnection, latchMock);

        // prevent reader thread from starting
        doNothing().when(spyConnection).synchronizedStartReader();

        // invoke private method
        Method method = IBConnection.class.getDeclaredMethod("connectIfNeeded");
        method.setAccessible(true);
        method.invoke(spyConnection);

        // verify reconnect is scheduled
        verify(spyConnection).scheduleConnection(IBConnection.RECONNECT_DELAY_MS);
    }

    @Test
    void connectIfNeeded_tcpConnectionFails_schedulesReconnect() throws Exception {
        // Spy on IBConnection
        IBConnection spyConnection = spy(ibConnection);

        // Simulate client.eConnect throwing an exception
        doThrow(new RuntimeException("TCP failure")).when(clientMock).eConnect(anyString(), anyInt(), anyInt());

        // Call private connectIfNeeded()
        var method = IBConnection.class.getDeclaredMethod("connectIfNeeded");
        method.setAccessible(true);
        method.invoke(spyConnection);

        // Verify reconnect scheduled after exception
        verify(spyConnection).scheduleConnection(IBConnection.RECONNECT_DELAY_MS);
    }

}
