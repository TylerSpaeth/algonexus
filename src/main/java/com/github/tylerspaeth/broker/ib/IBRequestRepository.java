package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.common.BuildableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for storing pending requests that are waiting for callbacks from IB.
 */
public class IBRequestRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBRequestRepository.class);

    private final Map<String, BuildableFuture<Object>> pendingRequests = new HashMap<>();

    // Map keys for requests that can only have a single request at a time
    public static final String POSITION_REQ_MAP_KEY = "PositionRequest";

    /**
     * Register a request ID that will be completed later.
     * @param reqId IB request ID
     * @return CompletableFuture for the request ID
     * @param <T> The type to be returned from the request
     */
    @SuppressWarnings("unchecked")
    public <T> BuildableFuture<T> registerPendingRequest(String reqId) {
        if(pendingRequests.containsKey(reqId)) {
            LOGGER.warn("Already a pending request for reqId {}", reqId);
            return null;
        }
        BuildableFuture<T> future = new BuildableFuture<>();
        pendingRequests.put(reqId, (BuildableFuture<Object>) future);
        return future;
    }

    /**
     * Remove a pending request from the map since it has been completed.
     * @param reqId IB request ID
     * @param <T> The type to be returned from the request
     */
    public <T> void removePendingRequest(String reqId) {
        BuildableFuture<Object> future = pendingRequests.remove(reqId);
        if(future != null) {
            future.complete();
        }
    }

    /**
     * Remover a pending request from the map since an error has occurred.
     * @param reqId IB request ID
     * @param throwable Exception that occurred
     */
    public void removePendingRequestWithException(String reqId, Throwable throwable) {
        BuildableFuture<Object> future = pendingRequests.remove(reqId);
        if(future != null) {
            future.completeExceptionally(throwable);
        }
    }

    /**
     * Gets the current value of the future at the given reqId.
     * @param reqId Id of the future
     * @return Current value stored in the future
     * @param <T> Type of value stored in the future
     */
    @SuppressWarnings("unchecked")
    public <T> T getFutureValue(String reqId) {
        BuildableFuture<T> future = (BuildableFuture<T>) pendingRequests.get(reqId);
        if(future != null) {
            return future.getValue();
        }
        return null;
    }

    /**
     * Sets the value stored in the future.
     * @param reqId Id of the future
     * @param value Value to store in the future
     * @param <T> Type of value to be stored in the future
     */
    @SuppressWarnings("unchecked")
    public <T> void setFutureValue(String reqId, T value) {
        BuildableFuture<T> future = (BuildableFuture<T>) pendingRequests.get(reqId);
        if(future != null) {
            future.setValue(value);
        }
    }

}