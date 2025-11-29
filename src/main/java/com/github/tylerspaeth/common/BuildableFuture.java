package com.github.tylerspaeth.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper around the CompletableFuture that allows the value to be set and modified multiple times before completion.
 * @param <T> The type that will be returned from the future
 */
public class BuildableFuture<T> {

    private final CompletableFuture<T> future = new CompletableFuture<>();
    private final AtomicReference<T> value = new AtomicReference<>(null);

    /**
     * Gets the current value that the future is storing.
     * @return T
     */
    public T getValue() {
        return value.get();
    }

    /**
     * Sets the value of the future.
     * @param t T
     */
    public void setValue(T t) {
        value.set(t);
    }

    /**
     * Wrapper around CompletableFuture.complete(T) that completes the future with current value of the future.
     */
    public void complete() {
        future.complete(value.get());
    }

    /**
     * Wrapper around CompletableFuture.completeExceptionally(Throwable) that completes the future with an exception.
     * @param ex The exception to complete the future with.
     */
    public void completeExceptionally(Throwable ex) {
        future.completeExceptionally(ex);
    }

    /**
     * Wrapper that just exposes the standard CompletableFuture.get(long, TimeUnit) method. {@link CompletableFuture#get(long, TimeUnit)}
     */
    public T get(long timeout, TimeUnit timeUnit) throws Exception {
        return future.get(timeout, timeUnit);
    }

}
