package com.github.tylerspaeth.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A queue that supports multiple readers and writers. Readers must be subscribed before they can start reading.
 * @param <T> The object the queue is being used for
 */
public class MultiReaderQueue<T> {

    public static final Logger LOGGER = LoggerFactory.getLogger(MultiReaderQueue.class);

    private final ConcurrentHashMap<UUID, ConcurrentLinkedQueue<T>> queues = new ConcurrentHashMap<>();

    /**
     * Writes to the queue for all readers to read.
     * @param t The object to put at the end of the queue.
     */
    public void write(T t) {
        Collection<ConcurrentLinkedQueue<T>> queuesSnapshot = queues.values();
        for(ConcurrentLinkedQueue<T> queue : queuesSnapshot) {
            queue.add(t);
        }
    }

    /**
     * Reads from the queue.
     * @param uuid The uuid of the reader.
     * @return The next object in the queue that the reader has yet to read.
     */
    public T read(UUID uuid) {
        ConcurrentLinkedQueue<T> queue = queues.get(uuid);
        if(queue == null) {
            return null;
        }
        return queue.poll();
    }

    /**
     * Reads a fixed number of items from the queue.
     * @param uuid The uuid of the reader.
     * @param desiredCount The number of objects to be read from the queue.
     * @return List with desiredCount objects if there are at least that many objects in the queue.
     * If there are not enough, then an empty list will be returned.
     */
    public List<T> read(UUID uuid, int desiredCount) {
        ConcurrentLinkedQueue<T> queue = queues.get(uuid);
        if(queue == null || queue.size() < desiredCount) {
            return new ArrayList<>();
        }
        List<T> items = new ArrayList<>();
        while(items.size() < desiredCount) {
            items.add(queue.poll());
        }
        return items;
    }

    /**
     * Gets all the unread objects in the queue for this reader.
     * @param uuid The uuid of the reader.
     * @return List of all unread objects.
     */
    public List<T> dump(UUID uuid) {
        ConcurrentLinkedQueue<T> queue = queues.get(uuid);
        if(queue == null) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        while(!queue.isEmpty()) {
            list.add(queue.poll());
        }
        return list;
    }

    /**
     * Subscribes a new reader to this queue.
     * @return UUID used by the reader for subsequent reading
     */
    public UUID subscribe() {
        UUID newUUID = UUID.randomUUID();
        queues.put(newUUID, new ConcurrentLinkedQueue<>());

        LOGGER.info("New subscription: {}.", newUUID);

        return newUUID;
    }

    /**
     * Unsubscribes a reader from this queue.
     * @param uuid the key the reader used for reading
     */
    public void unsubscribe(UUID uuid) {
        queues.remove(uuid);
        LOGGER.info("{} unsubscribed.", uuid);
    }

    /**
     * Gets the number of actively subscribed readers
     * @return number of actively subscribed readers
     */
    public int readerCount() {
        return queues.size();
    }

}
