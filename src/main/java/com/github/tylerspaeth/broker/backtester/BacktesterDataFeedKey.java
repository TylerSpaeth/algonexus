package com.github.tylerspaeth.broker.backtester;

/**
 * Key for identifying data feeds in the Backtester.
 * @param symbolID ID of the symbol that the data feed is for.
 * @param threadID ID for the thread that the data feed is being run on.
 */
public record BacktesterDataFeedKey(Integer symbolID, long threadID) {}
