package com.github.tylerspaeth.ib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Used for storing pending requests that are waiting for callbacks from IB.
 */
public class IBRequestRepository {

    private final Map<Integer, CompletableFuture<Object>> pendingRequests = new HashMap<>();

    /**
     * Register a request ID that will be completed later.
     * @param reqId IB request ID
     * @return CompletableFuture for the request ID
     * @param <T> The type to be returned from the request
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> registerPendingRequest(int reqId) {
        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(reqId, (CompletableFuture<Object>) future);
        return future;
    }

    /**
     * Remove a pending request from the map since it has been completed.
     * @param reqId IB request ID
     * @param result The value to complete the request with
     * @param <T> The type to be returned from the request
     */
    public <T> void removePendingRequest(int reqId, T result) {
        CompletableFuture<Object> future = pendingRequests.remove(reqId);
        if(future != null) {
            future.complete(result);
        }
    }

    /**
     * Remover a pending request from the map since an error has occurred.
     * @param reqId IB request ID
     * @param throwable Exception that occurred
     */
    public void removePendingRequestWithException(int reqId, Throwable throwable) {
        CompletableFuture<Object> future = pendingRequests.remove(reqId);
        if(future != null) {
            future.completeExceptionally(throwable);
        }
    }

}
