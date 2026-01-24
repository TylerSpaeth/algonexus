package com.github.tylerspaeth.engine.request;

import com.github.tylerspaeth.broker.service.IAccountService;
import com.github.tylerspaeth.broker.service.IDataFeedService;
import com.github.tylerspaeth.broker.service.IOrderService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Abstract class for all engine requests to be built from.
 * @param <T> Return type for the request.
 */
public abstract class AbstractEngineRequest<T> implements Runnable {

    protected IAccountService accountService;
    protected IDataFeedService dataFeedService;
    protected IOrderService orderService;

    private final CompletableFuture<T> future = new CompletableFuture<>();

    /**
     * Makes the request to the appropriate service.
     * @return Response from the service.
     */
    protected abstract T execute();

    @Override
    public void run() {
        try {
            T result = execute();
            future.complete(result);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    /**
     * Wrapper around the get method of the underlying CompletableFuture.
     * @return Response for request.
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public T get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    /**
     * Sets the services for the request to use. Should be called by the EngineCoordinator.
     * @param accountService IAccountService
     * @param dataFeedService IDataFeedService
     * @param orderService IOrderService
     */
    public void setServices(IAccountService accountService, IDataFeedService dataFeedService, IOrderService orderService) {
        this.accountService = accountService;
        this.dataFeedService = dataFeedService;
        this.orderService = orderService;
    }
}
