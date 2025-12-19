package com.github.tylerspaeth.broker.ib.service;

import com.github.tylerspaeth.broker.DataFeedKey;
import com.github.tylerspaeth.broker.service.IDataFeedService;
import com.github.tylerspaeth.broker.ib.IBMapper;
import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.broker.ib.response.RealtimeBar;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
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
    public void subscribeToDataFeed(Symbol symbol) {
        wrapper.subscribeToDataFeed(getDataFeedKeyFromSymbol(symbol));
    }

    @Override
    public List<Candlestick> readFromDataFeed(Symbol symbol, int intervalDuration, IntervalUnitEnum intervalUnit) {
        DataFeedKey dataFeedKey = getDataFeedKeyFromSymbol(symbol);
        List<RealtimeBar> realtimeBars = wrapper.readFromDataFeed(dataFeedKey, intervalDuration, intervalUnit);
        return realtimeBars.stream().map(IBMapper::mapRealTimeBarToCandlestick).collect(Collectors.toList());
    }

    @Override
    public void unsubscribeFromDataFeed(Symbol symbol) {
        wrapper.unsubscribeFromDataFeed(getDataFeedKeyFromSymbol(symbol));
    }

    /**
     * Builds a DataFeedKey from a Symbol.
     * @param symbol Symbol
     * @return DataFeedKey
     */
    private DataFeedKey getDataFeedKeyFromSymbol(Symbol symbol) {
        return new DataFeedKey(null, symbol.getTicker(), IBMapper.mapAssetTypeToSecType(symbol.getAssetType()).name(), symbol.getExchange().getName(), "USD");
    }

}
