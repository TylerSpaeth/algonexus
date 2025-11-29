package com.github.tylerspaeth.broker;

import com.github.tylerspaeth.broker.datastream.DataFeedKey;
import com.github.tylerspaeth.broker.response.ContractDetailsResponse;
import com.github.tylerspaeth.broker.response.OHLCV;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;

import java.util.List;
import java.util.UUID;

public interface IDataFeedService {

    /**
     * Search for symbols that the match the provided lookup values.
     * @param lookupValue String to match to ticker or security name
     * @return ContractDescriptions that match the given lookup value
     * @throws Exception if something fails while making the request
     */
    ContractDescription[] getMatchingSymbols(String lookupValue) throws Exception;

    /**
     * Gets complete details for a contract in the IB database.
     * @param contract Base contract to search for additional details for.
     * @return ContractDetailsResponse that will have a ContractDetails for each match found.
     * @throws Exception if something fails while making the request
     */
    ContractDetailsResponse getContractDetails(Contract contract) throws Exception;

    /**
     * Creates a new subscription that matches the given datafeed.
     * @param dataFeedKey Key defining the subscription.
     * @return UUID that is this subscription's unique key to the datafeed
     */
    UUID subscribeToDataFeed(DataFeedKey dataFeedKey);

    /**
     * Gathers all the data from the feed since it was last accessed.
     * @param dataFeedKey Defines the datafeed subscription
     * @param uuid the subscription's key for accessing the datafeed
     * @return List of all OHLCV data that has not been read yet.
     */
    List<OHLCV> readFromDataFeed(DataFeedKey dataFeedKey, UUID uuid);

    /**
     * Unsubscribes this subscriber from the data feed.
     * @param dataFeedKey Defines the datafeed subscription
     * @param uuid the subscription's key for accessing the datafeed
     */
    void unsubscribeFromDataFeed(DataFeedKey dataFeedKey, UUID uuid);


}
