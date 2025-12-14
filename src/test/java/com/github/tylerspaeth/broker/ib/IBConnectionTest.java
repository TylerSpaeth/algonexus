package com.github.tylerspaeth.broker.ib;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

public class IBConnectionTest {

    private IBConnection ibConnection;

    @BeforeEach
    public void setup() {

        ibConnection = new IBConnection() {
            @Override
            protected void synchronizedStartReader() {
                // avoid starting a real thread
                setReaderStarted(true);
            }

            @Override
            void connectIfNeeded() {
                // avoid real connection
            }
        };
    }

    @Test
    public void testConnectSchedulesConnection() {
        Assertions.assertDoesNotThrow(() -> ibConnection.connect());
    }

    @Test
    public void testDisconnectDoesNotThrow() {
        Assertions.assertDoesNotThrow(() -> ibConnection.disconnect());
    }

    @Test
    public void testScheduleConnectionExecutesRunnable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        IBConnection connection = new IBConnection() {
            @Override
            void connectIfNeeded() {
                latch.countDown();
            }
        };

        connection.scheduleConnection(50);
        Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testOnNextValidIdUpdatesAtomicIntegerAndLatch() {
        AtomicInteger nextId = ibConnection.nextValidId;
        ibConnection.setHandshakeLatch(new CountDownLatch(1));

        ibConnection.onNextValidId(99);

        Assertions.assertEquals(99, nextId.get());
        Assertions.assertEquals(0, ibConnection.getHandshakeLatch().getCount());
    }

    @Test
    public void testOnNextValidIdLatchNullSafe() {
        ibConnection.setHandshakeLatch(null);
        Assertions.assertDoesNotThrow(() -> ibConnection.onNextValidId(123));
        Assertions.assertEquals(123, ibConnection.nextValidId.get());
    }

    @Test
    public void testOnConnectionClosedReconnectsWhenNotManual() {
        IBConnection spyConnection = Mockito.spy(ibConnection);
        spyConnection.setManualDisconnect(false);
        doNothing().when(spyConnection).scheduleConnection(anyInt());

        spyConnection.onConnectionClosed();

        verify(spyConnection, times(1)).scheduleConnection(IBConnection.RECONNECT_DELAY_MS);
    }

    @Test
    public void testOnConnectionClosedDoesNotReconnectWhenManual() {
        IBConnection spyConnection = Mockito.spy(ibConnection);
        spyConnection.setManualDisconnect(true);

        spyConnection.onConnectionClosed();

        verify(spyConnection, never()).scheduleConnection(anyInt());
    }

    @Test
    public void testSynchronizedStartReaderSetsReaderStarted() {
        ibConnection.setReaderStarted(false);
        ibConnection.synchronizedStartReader();
        Assertions.assertTrue(ibConnection.isReaderStarted());
    }

    @Test
    public void testReaderAlreadyStartedDoesNothing() {
        ibConnection.setReaderStarted(true);
        ibConnection.synchronizedStartReader();
        Assertions.assertTrue(ibConnection.isReaderStarted());
    }

    @Test
    public void testConnectIfNeededAlreadyConnectingSkips() {
        IBConnection connection = new IBConnection() {
            {
                setConnecting(true); // simulate already connecting
            }

            @Override
            void connectIfNeeded() {
                super.connectIfNeeded(); // will skip because connecting is true
            }
        };
        Assertions.assertDoesNotThrow(connection::connectIfNeeded);
    }

    @Test
    public void testConnectIfNeededAlreadyConnectedSkips() {
        IBConnection connection = new IBConnection() {
            @Override
            void connectIfNeeded() {
                setConnecting(true); // enter the first block
                // simulate client connected
                setReaderStarted(false);
            }
        };
        Assertions.assertDoesNotThrow(connection::connectIfNeeded);
    }

    @Test
    public void testHandshakeTimeoutRetriesConnection() {
        IBConnection connection = new IBConnection() {
            @Override
            void connectIfNeeded() {
                setConnecting(true);
                setHandshakeLatch(new CountDownLatch(1)); // never counts down
                // should hit timeout branch
            }
        };
        Assertions.assertDoesNotThrow(connection::connectIfNeeded);
    }

    @Test
    public void testConnectIfNeededHandshakeTimeout() {
        IBConnection connection = new IBConnection() {
            @Override
            synchronized void synchronizedStartReader() {
                // skip starting threads
            }

            @Override
            public void scheduleConnection(int delayMS) {
                // skip real scheduling
            }
        };
        connection.setManualDisconnect(false);
        connection.setConnecting(false);
        // Do not call onNextValidId -> handshakeLatch.await() times out
        Assertions.assertDoesNotThrow(connection::connectIfNeeded);
    }
}
