package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.IAccountService;
import com.github.tylerspaeth.broker.IDataFeedService;
import com.github.tylerspaeth.broker.IOrderService;
import com.github.tylerspaeth.broker.response.OrderResponse;
import com.github.tylerspaeth.broker.datastream.DataFeedKey;
import com.github.tylerspaeth.common.MultiReaderQueue;
import com.github.tylerspaeth.broker.response.*;
import com.github.tylerspaeth.common.BuildableFuture;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.github.tylerspaeth.common.enums.MarketDataType;
import com.ib.client.*;
import com.ib.controller.AccountSummaryTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IBService implements IAccountService, IDataFeedService, IOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBService.class);

    private static IBService instance;

    private final IBConnection ibConnection = new IBConnection();

    private static final int REQ_TIMEOUT_MS = 5000;

    private IBService() {}

    /**
     * For getting the singleton instance of this class. This is the only way that an IBService instance should be
     * acquired in normal applications.
     * @return Singleton instance of this class
     */
    public static IBService getInstance() {
        if(instance == null) {
            instance = new IBService();
        }
        return instance;
    }

    /**
     * For getting a new IBService instance to use for testing. This should NOT be used for anything other than testing.
     * @return IBService
     */
    public static IBService getInstanceTest() {
        return new IBService();
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

    public void setDataType(MarketDataType marketDataType) {
        ibConnection.client.reqMarketDataType(marketDataType.code);
    }

    // ACCOUNT

    @Override
    public AccountSummaryResponse getAccountSummary(List<AccountSummaryTag> accountSummaryTags) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<AccountSummaryResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        List<String> tags = accountSummaryTags.stream().map(Enum::name).toList();
        ibConnection.client.reqAccountSummary(reqId, "All", String.join(",", tags));
        AccountSummaryResponse response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelAccountSummary(reqId);
        }
        return response;
    }

    @Override
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

    @Override
    public AccountPnLResponse getAccountPnL(String accountId) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<AccountPnLResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPnL(reqId, accountId, "");
        AccountPnLResponse response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelPnL(reqId);
        }
        return response;
    }

    @Override
    public PositionPnLResponse getPositionPnL(String accountId, int conId) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<PositionPnLResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPnLSingle(reqId, accountId, "", conId);
        PositionPnLResponse response;
        try {
            response = future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } finally {
            ibConnection.client.cancelPnLSingle(reqId);
        }
        return response;
    }

    // DATAFEED

    @Override
    public ContractDescription[] getMatchingSymbols(String lookupValue) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<ContractDescription[]> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqMatchingSymbols(reqId, lookupValue);
        return future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public ContractDetailsResponse getContractDetails(Contract contract) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<ContractDetailsResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqContractDetails(reqId, contract);
        return future.get(REQ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public UUID subscribeToDataFeed(DataFeedKey dataFeedKey) {
        MultiReaderQueue<OHLCV> queue = ibConnection.datafeeds.get(dataFeedKey);

        ibConnection.client.reqMarketDataType(4);

        if (queue == null) {

            // Create the new queue
            int reqId = ibConnection.nextValidId.getAndIncrement();
            dataFeedKey.setReqId(reqId);
            MultiReaderQueue<OHLCV> newQueue = new MultiReaderQueue<>();
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

        return queue.subscribe();
    }

    @Override
    public List<OHLCV> readFromDataFeed(DataFeedKey dataFeedKey, UUID uuid, int intervalDuration, IntervalUnitEnum intervalUnit) {
        MultiReaderQueue<OHLCV> queue = ibConnection.datafeeds.get(dataFeedKey);

        if(queue == null) {
            LOGGER.warn("Unable to find queue for {}", dataFeedKey);
            return new ArrayList<>();
        }

        if((intervalDuration * intervalUnit.secondsPer) % 5 != 0) {
            LOGGER.error("Invalid intervalDuration and intervalUnit provided.");
            return new ArrayList<>();
        }

        // Build the list of candles to return, combining existing candles if need be
        int numToCondense = intervalDuration * intervalUnit.secondsPer / 5;
        List<OHLCV> itemsToReturn = new ArrayList<>();
        List<OHLCV> itemsToCondense;
        do {
            itemsToCondense = queue.read(uuid, numToCondense);
            if(!itemsToCondense.isEmpty()) {
                // Aggregate data from all the OHLCV candles that are being combined into one
                Timestamp time = itemsToCondense.getFirst().time();
                double open = itemsToCondense.getLast().open();
                double high = itemsToCondense.getFirst().high();
                double low = itemsToCondense.getFirst().low();
                double close = itemsToCondense.getLast().close();
                double volume = itemsToCondense.getFirst().volume();
                for(int i = 1; i < itemsToCondense.size(); i++) {
                    OHLCV item = itemsToCondense.get(i);
                    high = Math.max(high, item.high());
                    low = Math.min(low, item.low());
                    volume += item.volume();
                }
                itemsToReturn.add(new OHLCV(time, open, high, low, close, volume));
            }
        } while(!itemsToCondense.isEmpty());

        return itemsToReturn;
    }

    @Override
    public void unsubscribeFromDataFeed(DataFeedKey dataFeedKey, UUID uuid) {
        MultiReaderQueue<OHLCV> queue = ibConnection.datafeeds.get(dataFeedKey);

        if(queue != null) {
            queue.unsubscribe(uuid);

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

    // ORDER

    @Override
    public OrderResponse placeOrder(Contract contract, Order order) {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        OrderResponse state = new OrderResponse(reqId, contract, order);
        ibConnection.orderStateMap.put(reqId, state);
        ibConnection.client.placeOrder(reqId, contract, order);
        return state;
    }

    @Override
    public OrderCancel cancelOrder(int orderID) {
        OrderCancel orderCancel = new OrderCancel();
        ibConnection.client.cancelOrder(orderID, orderCancel);
        return orderCancel;
    }

}
