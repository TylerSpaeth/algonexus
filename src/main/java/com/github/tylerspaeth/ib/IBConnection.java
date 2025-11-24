package com.github.tylerspaeth.ib;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IBConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBConnection.class);

    private final IBWrapper wrapper = new IBWrapper(this);
    private final EJavaSignal signal = new EJavaSignal();
    public final EClientSocket client = new EClientSocket(wrapper, signal);

    private ScheduledExecutorService scheduler;

    public IBRequestRepository ibRequestRepository = new IBRequestRepository();
    private AtomicInteger nextValidId = new AtomicInteger();

    // Synchronization
    private final AtomicBoolean readerStarted = new AtomicBoolean(false);
    private final AtomicBoolean manualDisconnect = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private volatile CountDownLatch handshakeLatch;

    public static final int RECONNECT_DELAY_MS = 5000;
    private static final int MAX_TCP_CONNECTION_WAIT_TIME_MS = 1000;
    private static final int MAX_HANDSHAKE_TIMEOUT_DURATION_MS = 1000;

    /**
     * Initialized a TCP connection via TWS.
     */
    public void connect() {
        manualDisconnect.set(false);
        scheduleConnection(0);
    }

    /**
     * Disconnects from the IB TWS connection.
     */
    public void disconnect() {
        manualDisconnect.set(true);
        if(scheduler != null) {
            scheduler.shutdownNow();
        }
        synchronizedDisconnect();
    }

    /**
     * Schedules a connection to be attempted.
     * @param delayMS How long to wait before attempting the connection.
     */
    public void scheduleConnection(int delayMS) {
        restartScheduler();
        scheduler.schedule(this::connectIfNeeded, delayMS, TimeUnit.MILLISECONDS);
    }

    /**
     * Attempts the TWS connection if it is needed.
     */
    private void connectIfNeeded() {
        if(manualDisconnect.get()) {
            return;
        }

        if(!connecting.compareAndSet(false, true)) {
            LOGGER.warn("Already connecting, skipping.");
            return;
        }

        try {
            if(client.isConnected()) {
                LOGGER.warn("Already connected, skipping.");
                return;
            }

            handshakeLatch = new CountDownLatch(1);
            client.eConnect("127.0.0.1", 4002, 1);

            int waited = 0;
            while(!client.isConnected() && waited < MAX_TCP_CONNECTION_WAIT_TIME_MS) {
                try {
                    Thread.sleep(100);
                    waited += 100;
                } catch(InterruptedException _) {}
            }

            if(!client.isConnected()) {
                LOGGER.warn("TCP connection not initialized in time, retrying in {} seconds", (float) RECONNECT_DELAY_MS / 1000);
                scheduleConnection(RECONNECT_DELAY_MS);
                return;
            }

            synchronizedStartReader();

            boolean handshakeGood = false;
            try {
                handshakeGood = handshakeLatch.await(MAX_HANDSHAKE_TIMEOUT_DURATION_MS, TimeUnit.MILLISECONDS);
            } catch(InterruptedException _) {}

            if(!handshakeGood) {
                LOGGER.warn("Handshake failed, disconnecting and retrying in {} seconds.", (float) RECONNECT_DELAY_MS / 1000);
                synchronizedDisconnect();
                scheduleConnection(RECONNECT_DELAY_MS);
            }
        } catch (Exception e) {
            LOGGER.warn("Connection failed, retrying connection in {} seconds:", (float) RECONNECT_DELAY_MS / 1000, e);
            synchronizedDisconnect();
            scheduleConnection(RECONNECT_DELAY_MS);
        } finally {
            connecting.set(false);
        }
    }

    /**
     * Starts the TWS-Reader thread if it is not already started.
     */
    synchronized void synchronizedStartReader() {
        if(readerStarted.get()) {
            LOGGER.warn("Reader already started, skipping.");
            return;
        }

        EReader reader = new EReader(client, signal);
        reader.start();

        Thread readerThread = new Thread(() -> {
            try {
                while (client.isConnected()) {
                    signal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (IOException e) {
                        LOGGER.error("processMsgs failed: ", e);
                    }
                }
            } finally {
                readerStarted.set(false);
                LOGGER.info("Closing IB-Reader.");
            }
        }, "IB-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
        readerStarted.set(true);
        LOGGER.info("IB-Reader Started");
    }

    /**
     * Thread safe disconnection from TWS.
     */
    private synchronized void synchronizedDisconnect() {
        try {
            if(client.isConnected()) {
                client.eDisconnect();
            }
        } catch (Exception e) {
            LOGGER.error("Error disconnecting: ", e);
        } finally {
            readerStarted.set(false);
        }
    }

    /**
     * Restarts the scheduler if it has not been created or is not running
     */
    private void restartScheduler() {
        if(scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "IB-Connector");
                t.setDaemon(true);
                return t;
            });
        }
    }

    /**
     * To be called by the EWrapper connectAck callback.
     */
    public void onConnectAck() {
        LOGGER.info("IB Connected");
    }

    /**
     * To be called by the EWrapper connectionClosed callback.
     */
    public void onConnectionClosed() {
        LOGGER.info("IB Disconnected");
        if(manualDisconnect.get()) {
            return;
        }
        scheduleConnection(IBConnection.RECONNECT_DELAY_MS);
    }

    /**
     * To be called by the nextValidId callback
     * @param i The next valid id
     */
    public void onNextValidId(int i) {
        if (handshakeLatch != null && handshakeLatch.getCount() > 0) {
            handshakeLatch.countDown();
        }
        nextValidId.set(i);
    }


}
