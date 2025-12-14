package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.DataFeedKey;
import com.github.tylerspaeth.broker.ib.response.AccountPnL;
import com.github.tylerspaeth.broker.ib.response.AccountSummary;
import com.github.tylerspaeth.broker.ib.response.OrderResponse;
import com.github.tylerspaeth.broker.ib.response.Position;
import com.github.tylerspaeth.broker.ib.response.PositionPnL;
import com.github.tylerspaeth.broker.ib.response.RealtimeBar;
import com.github.tylerspaeth.common.MultiReaderQueue;
import com.github.tylerspaeth.common.BuildableFuture;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.ib.client.*;
import com.ib.controller.AccountSummaryTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A synchronous wrapper around the IB TWS-API functionality.
 */
public class IBSyncWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBSyncWrapper.class);

    private static IBSyncWrapper instance;

    private final IBConnection ibConnection;

    private static final int REQ_TIMEOUT_MS = 5000;

    private IBSyncWrapper(IBConnection ibConnection) {
        this.ibConnection = ibConnection;
    }

    /**
     * For getting the singleton instance of this class. This is the only way that an IBService instance should be
     * acquired in normal applications.
     * @return Singleton instance of this class
     */
    public static IBSyncWrapper getInstance() {
        if(instance == null) {
            instance = new IBSyncWrapper(new IBConnection());
        }
        return instance;
    }

    /**
     * For getting a new IBService instance to use for testing. This should NOT be used for anything other than testing.
     * @return IBService
     */
    public static IBSyncWrapper getInstanceTest(IBConnection ibConnection) {
        return new IBSyncWrapper(ibConnection);
    }

    public void connect() {
        ibConnection.connect();
    }

    public void disconnect() {
        ibConnection.disconnect();
    }

    public boolean isConnected() {
        return ibConnection.client.isConnected();
    }

    /**
     * Sets the type of market data we will receive
     * @param marketDataType The code for the type of market data
     */
    public void setDataType(int marketDataType) {
        ibConnection.client.reqMarketDataType(marketDataType);
    }

    /**
     * Get account information.
     * @param group The group of accounts to search for
     * @param accountSummaryTags The tags for the information to receive
     * @return List of AccountSummary objects
     * @throws Exception if something fails while making the request
     */
    public List<AccountSummary> getAccountSummary(String group, List<AccountSummaryTag> accountSummaryTags) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<List<AccountSummary>> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        List<String> tags = accountSummaryTags.stream().map(Enum::name).toList();
        ibConnection.client.reqAccountSummary(reqId, group, String.join(",", tags));
        List<AccountSummary> response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelAccountSummary(reqId);
        }
        return response;
    }

    /**
     * Gets all the positions for the active account
     * @return list of Positions
     * @throws Exception if something fails while making the request
     */
    public List<Position> getPositions() throws Exception {
        String reqId = IBRequestRepository.POSITION_REQ_MAP_KEY;
        BuildableFuture<List<Position>> future = ibConnection.ibRequestRepository.registerPendingRequest(reqId);
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPositions();
        List<Position> response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelPositions();
        }
        return response;
    }

    /**
     * Get PnL information across the entire account
     * @param accountId ID of the account
     * @return PnL information for the account
     * @throws Exception if something fails while making the request
     */
    public AccountPnL getAccountPnL(String accountId, String modelCode) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<AccountPnL> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPnL(reqId, accountId, modelCode);
        AccountPnL response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelPnL(reqId);
        }
        return response;
    }

    /**
     * Get PnL information for a specific position.
     * @param accountId the accountID
     * @param conId the contractID
     * @return PnL information for the position
     * @throws Exception if something fails while making the request
     */
    public PositionPnL getPositionPnL(String accountId, String modelCode, int conId) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<PositionPnL> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPnLSingle(reqId, accountId, modelCode, conId);
        PositionPnL response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelPnLSingle(reqId);
        }
        return response;
    }

    /**
     * THIS IS CURRENTLY FOR DEBUG USE ONLY
     * <p>
     * Search for symbols that the match the provided lookup values.
     * @param lookupValue String to match to ticker or security name
     * @return ContractDescriptions that match the given lookup value
     * @throws Exception if something fails while making the request
     */
    public ContractDescription[] getMatchingSymbols(String lookupValue) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<ContractDescription[]> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqMatchingSymbols(reqId, lookupValue);
        return future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * THIS IS CURRENTLY FOR DEBUG USE ONLY
     * <p>
     * Gets complete details for a contract in the IB database.
     * @param contract Base contract to search for additional details for.
     * @return List of ContractDetails that will have a ContractDetails for each match found.
     * @throws Exception if something fails while making the request
     */
    public List<ContractDetails> getContractDetails(Contract contract) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<List<ContractDetails>> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqContractDetails(reqId, contract);
        return future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new subscription that matches the given datafeed.
     * @param dataFeedKey Key defining the subscription.
     */
    public void subscribeToDataFeed(DataFeedKey dataFeedKey) {
        MultiReaderQueue<RealtimeBar> queue = ibConnection.datafeeds.get(dataFeedKey);

        if (queue == null) {

            // Create the new queue
            int reqId = ibConnection.nextValidId.getAndIncrement();
            dataFeedKey.setReqId(reqId);
            MultiReaderQueue<RealtimeBar> newQueue = new MultiReaderQueue<>();
            ibConnection.datafeeds.put(dataFeedKey, newQueue);
            ibConnection.datafeedReqIdMap.put(reqId, newQueue);
            queue = newQueue;

            // Make IB request
            Contract contract = new Contract();
            contract.symbol(dataFeedKey.getTicker());
            contract.secType(dataFeedKey.getSecType());
            contract.exchange(dataFeedKey.getExchange());
            contract.currency(dataFeedKey.getCurrency());
            ibConnection.client.reqRealTimeBars(reqId, contract, 5, "MIDPOINT", false, null);
        }

        queue.subscribe();
    }

    /**
     * Gathers all the data from the feed since it was last accessed.
     * @param dataFeedKey Defines the datafeed subscription
     * @param intervalDuration used in determining the granularity of the data to retrieve
     * @param intervalUnit used in determining the granularity of the data to retrieve
     * @return List of all OHLCV data that has not been read yet.
     */
    public List<RealtimeBar> readFromDataFeed(DataFeedKey dataFeedKey, int intervalDuration, IntervalUnitEnum intervalUnit) {
        MultiReaderQueue<RealtimeBar> queue = ibConnection.datafeeds.get(dataFeedKey);

        if(queue == null) {
            LOGGER.warn("Unable to find queue for {}", dataFeedKey);
            return new ArrayList<>();
        }

        if((intervalDuration * intervalUnit.secondsPer) % 5 != 0) {
            LOGGER.error("Invalid intervalDuration and intervalUnit provided.");
            return new ArrayList<>();
        }

        // Align the data with the start of a boundary
        while(queue.peek() != null && queue.peek().date() % ((long) intervalDuration * intervalUnit.secondsPer) != 0) {
            queue.read();
        }

        // Build the list of candles to return, combining existing candles if need be
        int numToCondense = intervalDuration * intervalUnit.secondsPer / 5;
        List<RealtimeBar> itemsToReturn = new ArrayList<>();
        List<RealtimeBar> itemsToCondense;
        do {
            itemsToCondense = queue.read(numToCondense);
            if(!itemsToCondense.isEmpty()) {
                // Aggregate data from all the bars that are being combined into one
                long date = itemsToCondense.getFirst().date();
                double open = itemsToCondense.getFirst().open();
                double high = itemsToCondense.getFirst().high();
                double low = itemsToCondense.getFirst().low();
                double close = itemsToCondense.getLast().close();
                Decimal volume = itemsToCondense.getFirst().volume();
                for(int i = 1; i < itemsToCondense.size(); i++) {
                    RealtimeBar item = itemsToCondense.get(i);
                    high = Math.max(high, item.high());
                    low = Math.min(low, item.low());
                    volume = volume.add(item.volume());
                }
                itemsToReturn.add(new RealtimeBar(date, open, high, low, close, volume));
            }
        } while(!itemsToCondense.isEmpty());

        return itemsToReturn;
    }

    /**
     * Unsubscribes this subscriber from the data feed.
     * @param dataFeedKey Defines the datafeed subscription
     */
    public void unsubscribeFromDataFeed(DataFeedKey dataFeedKey) {
        MultiReaderQueue<RealtimeBar> queue = ibConnection.datafeeds.get(dataFeedKey);

        if(queue != null) {
            queue.unsubscribe();

            // If there are no more reader reading from the queue, cancel the IB request and delete the data feed
            if(queue.readerCount() == 0) {

                DataFeedKey storedKey = null;
                for(var pairs : ibConnection.datafeeds.entrySet()) {
                    if(pairs.getKey().equals(dataFeedKey)) {
                        storedKey = pairs.getKey();
                    }
                }
                ibConnection.client.cancelRealTimeBars(Objects.requireNonNull(storedKey).getReqId());
                ibConnection.datafeeds.remove(dataFeedKey);
                ibConnection.datafeedReqIdMap.remove(storedKey.getReqId());
            }
        }
    }

    /**
     * Places an order and returns an object to monitor the state of the order.
     * @param contract The contract to open the order on
     * @param order The actual details of the order to open
     * @return OrderResponse object with details of the order's current state
     */
    public OrderResponse placeOrder(Contract contract, Order order) {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        OrderResponse state = new OrderResponse(reqId, contract, order);
        ibConnection.orderStateMap.put(reqId, state);
        ibConnection.client.placeOrder(reqId, contract, order);
        return state;
    }

    /**
     * Cancels an order.
     * @param orderID The id of the order to cancel.
     * @return OrderCancel object with details of order's cancellation
     */
    public OrderCancel cancelOrder(int orderID) {
        OrderCancel orderCancel = new OrderCancel();
        ibConnection.client.cancelOrder(orderID, orderCancel);
        return orderCancel;
    }

}
