package com.github.tylerspaeth.broker.ib;

import com.github.tylerspaeth.broker.IAccountService;
import com.github.tylerspaeth.broker.IDataFeedService;
import com.github.tylerspaeth.broker.IOrderService;
import com.github.tylerspaeth.broker.OrderState;
import com.github.tylerspaeth.broker.datastream.DataFeedKey;
import com.github.tylerspaeth.common.MultiReaderQueue;
import com.github.tylerspaeth.broker.request.AccountSummaryRequest;
import com.github.tylerspaeth.broker.request.PositionPnLRequest;
import com.github.tylerspaeth.broker.response.*;
import com.github.tylerspaeth.common.BuildableFuture;
import com.github.tylerspaeth.common.enums.MarketDataType;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.Order;
import com.ib.client.OrderCancel;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IBService implements IAccountService, IDataFeedService, IOrderService {

    private final IBConnection ibConnection = new IBConnection();

    private static final int REQ_TIMEOUT_MS = 5000;

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
    public AccountSummaryResponse getAccountSummary(AccountSummaryRequest accountSummaryRequest) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<AccountSummaryResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        List<String> tags = accountSummaryRequest.accountSummaryTags().stream().map(Enum::name).toList();
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
    public PositionResponse getPositions() throws Exception {
        String reqId = IBRequestRepository.POSITION_REQ_MAP_KEY;
        BuildableFuture<PositionResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(reqId);
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPositions();
        PositionResponse response;
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
    public PositionPnLResponse getPositionPnL(PositionPnLRequest positionPnLRequest) throws Exception {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        BuildableFuture<PositionPnLResponse> future = ibConnection.ibRequestRepository.registerPendingRequest(String.valueOf(reqId));
        if(future == null) {
            return null;
        }
        ibConnection.client.reqPnLSingle(reqId, positionPnLRequest.accountId(), "", positionPnLRequest.conId());
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
            contract.symbol(dataFeedKey.ticker());
            contract.secType(dataFeedKey.secType());
            contract.exchange(dataFeedKey.exchange());
            contract.currency(dataFeedKey.currency());
            ibConnection.client.reqRealTimeBars(reqId, contract, 5, "MIDPOINT", false, null);
        }

        return queue.subscribe();
    }

    @Override
    public List<OHLCV> readFromDataFeed(DataFeedKey dataFeedKey, UUID uuid) {
        MultiReaderQueue<OHLCV> queue = ibConnection.datafeeds.get(dataFeedKey);

        if(queue == null) {
            return null;
        }

        return queue.dump(uuid);
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
                ibConnection.client.cancelRealTimeBars(storedKey.reqId());
                ibConnection.datafeeds.remove(dataFeedKey);
                ibConnection.datafeedReqIdMap.remove(storedKey.getReqId());
            }
        }
    }

    // ORDER

    @Override
    public OrderState placeOrder(Contract contract, Order order) {
        int reqId = ibConnection.nextValidId.getAndIncrement();
        OrderState state = new OrderState(reqId, contract, order);
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
