package com.github.tylerspaeth.broker.ib.service;

import com.github.tylerspaeth.broker.ib.IBDataFeedKey;
import com.github.tylerspaeth.broker.ib.response.ContractDetails;
import com.github.tylerspaeth.broker.service.IDataFeedService;
import com.github.tylerspaeth.broker.ib.IBMapper;
import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.broker.ib.response.RealtimeBar;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.ib.client.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class IBDataFeedService implements IDataFeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBDataFeedService.class);

    private final IBSyncWrapper wrapper;

    public IBDataFeedService() {
        wrapper = IBSyncWrapper.getInstance();
    }

    @Override
    public void subscribeToDataFeed(long threadID, Symbol symbol) {
        wrapper.subscribeToDataFeed(threadID, getDataFeedKeyFromSymbol(symbol));
    }

    @Override
    public List<Candlestick> readFromDataFeed(long threadID, Symbol symbol, int intervalDuration, IntervalUnitEnum intervalUnit) {
        IBDataFeedKey dataFeedKey = getDataFeedKeyFromSymbol(symbol);
        List<RealtimeBar> realtimeBars = wrapper.readFromDataFeed(threadID, dataFeedKey, intervalDuration, intervalUnit);
        return realtimeBars.stream().map(IBMapper::mapRealTimeBarToCandlestick).collect(Collectors.toList());
    }

    @Override
    public void unsubscribeFromDataFeed(long threadID, Symbol symbol) {
        wrapper.unsubscribeFromDataFeed(threadID, getDataFeedKeyFromSymbol(symbol));
    }

    @Override
    public List<ContractDetails> getContractDetailsForSymbol(Symbol symbol) {
        Contract contract = new Contract();
        contract.symbol(symbol.getTicker());
        contract.exchange(symbol.getExchange().getName());
        contract.secType(IBMapper.mapAssetTypeToSecType(symbol.getAssetType()));
        contract.currency("USD");
        try {
            List<com.ib.client.ContractDetails> ibcontractDetails = wrapper.getContractDetails(contract);
            return ibcontractDetails.stream().map(IBMapper::mapIBContractDetails).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Error getting contract details for symbol: {}", symbol);
            return List.of();
        }
    }

    /**
     * Builds a DataFeedKey from a Symbol.
     * @param symbol Symbol
     * @return DataFeedKey
     */
    private IBDataFeedKey getDataFeedKeyFromSymbol(Symbol symbol) {
        return new IBDataFeedKey(null, symbol.getTicker(), IBMapper.mapAssetTypeToSecType(symbol.getAssetType()).name(), symbol.getExchange().getName(), "USD");
    }

}
