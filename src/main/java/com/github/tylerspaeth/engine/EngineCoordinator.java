package com.github.tylerspaeth.engine;

import com.github.tylerspaeth.broker.backtester.BacktesterDataFeedService;
import com.github.tylerspaeth.broker.backtester.BacktesterOrderService;
import com.github.tylerspaeth.broker.ib.service.IBAccountService;
import com.github.tylerspaeth.broker.ib.service.IBDataFeedService;
import com.github.tylerspaeth.broker.ib.service.IBOrderService;
import com.github.tylerspaeth.broker.service.IAccountService;
import com.github.tylerspaeth.broker.service.IDataFeedService;
import com.github.tylerspaeth.broker.service.IOrderService;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * The engine that handles coordination between strategies, UI, IB, and backtesting.
 */
public class EngineCoordinator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineCoordinator.class);

    private static final int MAX_SHUTDOWN_TIME_SEC = 10;

    private final ExecutorService executorService;
    private final BlockingQueue<AbstractEngineRequest<?>> requestQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = false;

    private final IBAccountService ibAccountService;
    private final IBDataFeedService ibDataFeedService;
    private final IBOrderService ibOrderService;

    private final BacktesterDataFeedService backtesterDataFeedService;
    private final BacktesterOrderService backtesterOrderService;

    // Set depending on if we are currently backtesting or running live
    private IAccountService activeAccountService;
    private IDataFeedService activeDataFeedService;
    private IOrderService activeOrderService;

    public EngineCoordinator(ExecutorService executorService, IBAccountService ibAccountService,
                             IBDataFeedService ibDataFeedService, IBOrderService ibOrderService,
                             BacktesterDataFeedService backtesterDataFeedService, BacktesterOrderService backtesterOrderService) {
        this.executorService = executorService;
        this.ibAccountService = ibAccountService;
        this.ibDataFeedService = ibDataFeedService;
        this.ibOrderService = ibOrderService;
        this.backtesterDataFeedService = backtesterDataFeedService;
        this.backtesterOrderService = backtesterOrderService;

        // Default to running live trading
        activeAccountService = ibAccountService;
        activeDataFeedService = ibDataFeedService;
        activeOrderService = ibOrderService;
    }

    /**
     * Submits a request and blocks until the request returns.
     * @param request Request to be executed by the engine.
     * @return Results of the request.
     * @param <T> Type to be returned by the request.
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    public <T> T submitRequest(AbstractEngineRequest<T> request) throws InterruptedException, ExecutionException {

        if(request == null) {
            LOGGER.warn("Request is null, nothing to process.");
            return null;
        }

        request.setServices(activeAccountService, activeDataFeedService, activeOrderService);
        requestQueue.put(request);
        return request.get();
    }

    /**
     * Runs the engine. Should be called from a dedicated thread.
     */
    public void run()  {
        running = true;
        while(running) {
            try {
                AbstractEngineRequest<?> request = requestQueue.poll(50, TimeUnit.MILLISECONDS);
                if(request != null) {
                    executorService.submit(request);
                }
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        stopExecutorService();
    }

    /**
     * Stops the engine.
     */
    public void stop() {
        running = false;
    }

    /**
     * Shuts down the executor service, giving pending requests a chance to complete but forcing shutdown if they take too long.
     */
    private void stopExecutorService() {

        while(!requestQueue.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(MAX_SHUTDOWN_TIME_SEC, TimeUnit.SECONDS)) {
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Enable use of the Backtester services.
     */
    public void useBacktester() {
        activeAccountService = null;
        activeDataFeedService = backtesterDataFeedService;
        activeOrderService = backtesterOrderService;
    }

    /**
     * Enable use of the IB services.
     */
    public void useIB() {
        activeAccountService = ibAccountService;
        activeDataFeedService = ibDataFeedService;
        activeOrderService =  ibOrderService;
    }
}
