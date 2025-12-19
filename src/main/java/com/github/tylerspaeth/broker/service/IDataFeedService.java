package com.github.tylerspaeth.broker.service;

import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;

import java.util.List;

/**
 * Access to data feeds and related functionality
 */
public interface IDataFeedService {

    /**
     * Subscribes to a data feed for the provided Symbol.
     * @param symbol Symbol to get data for.
     */
    void subscribeToDataFeed(Symbol symbol);

    /**
     * Gets unread Candlesticks from the data feed of the provided Symbol. Must be subscribed before reading.
     * @param symbol Symbol to get data for.
     * @param intervalDuration Used for determining the granularity of the Candlesticks.
     * @param intervalUnit Used for determine the granularity of the Candlesticks.
     * @return List of unread Candlesticks at the desired granularity.
     */
    List<Candlestick> readFromDataFeed(Symbol symbol, int intervalDuration, IntervalUnitEnum intervalUnit);

    /**
     * Unsubscribe from the data feed for the provided Symbol.
     * @param symbol Symbol to get data for.
     */
    void unsubscribeFromDataFeed(Symbol symbol);

}
