package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.broker.ib.response.ContractDetails;
import com.github.tylerspaeth.broker.service.IDataFeedService;
import com.github.tylerspaeth.common.data.dao.CandlestickDAO;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BacktesterDataFeedService implements IDataFeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterDataFeedService.class);

    private static final Integer MAX_CANDLESTICKS = 1_000_000;

    private final SymbolDAO symbolDAO;
    private final CandlestickDAO candlestickDAO;

    private final Map<BacktesterDataFeedKey, List<HistoricalDataset>> datafeeds = new ConcurrentHashMap<>(); // Used for querying more data
    private final Map<BacktesterDataFeedKey, Timestamp> lastSeenOffsets = new ConcurrentHashMap<>(); // Last seen timestamp offset for each datafeed
    private final Map<BacktesterDataFeedKey, Integer> datafeedIntervalMap = new ConcurrentHashMap<>(); // Map of the interval duration being read by each data feed
    private final Map<BacktesterDataFeedKey, IntervalUnitEnum> dataFeedIntervalUnitMap = new ConcurrentHashMap<>(); // Map of the interval unit being read by each data feed
    private final Map<BacktesterDataFeedKey, Deque<Candlestick>> candlesticksPendingReturn = new ConcurrentHashMap<>(); // Map of candlesticks that have already been built for each data feed
    private final Map<BacktesterDataFeedKey, Object> datafeedLocks; // Locks for accessing the shared service

    private final BacktesterSharedService backtesterSharedService;

    public BacktesterDataFeedService(BacktesterSharedService backtesterSharedService, SymbolDAO symbolDAO, CandlestickDAO candlestickDAO, ConcurrentHashMap<BacktesterDataFeedKey, Object> datafeedLocks) {
        this.backtesterSharedService = backtesterSharedService;
        this.symbolDAO = symbolDAO;
        this.candlestickDAO = candlestickDAO;
        this.datafeedLocks = datafeedLocks;
    }

    @Override
    public void subscribeToDataFeed(long threadID, Symbol symbol) {
        Symbol persistedSymbol = symbolDAO.getPersistedVersionOfSymbol(symbol);
        if(persistedSymbol == null) {
            LOGGER.error("Symbol {} is not persisted, therefore no data feed can be subscribed to.", symbol);
            return;
        }

        List<HistoricalDataset> historicalDatasets = persistedSymbol.getHistoricalDatasets();
        if(historicalDatasets == null || historicalDatasets.isEmpty()) {
            LOGGER.error("No HistoricalDatasets found for Symbol {}", symbol);
            return;
        }

        BacktesterDataFeedKey mapKey = new BacktesterDataFeedKey(persistedSymbol.getSymbolID(), threadID);

        datafeeds.put(mapKey, historicalDatasets);
        lastSeenOffsets.put(mapKey, Timestamp.from(Instant.EPOCH));
    }

    @Override
    public List<Candlestick> readFromDataFeed(long threadID, Symbol symbol, int intervalDuration, IntervalUnitEnum intervalUnit) {

        // This implementation will use the feed that matches the desired interval duration and unit if possible. Otherwise,
        // it will take the least granular.

        Symbol persistedSymbol = symbolDAO.getPersistedVersionOfSymbol(symbol);
        if (persistedSymbol == null) {
            LOGGER.error("Symbol {} is not persisted, therefore no data feed can be read from.", symbol);
            return List.of();
        }

        BacktesterDataFeedKey mapKey = new BacktesterDataFeedKey(persistedSymbol.getSymbolID(), threadID);

        List<HistoricalDataset> datasets = datafeeds.get(mapKey);
        if (datasets == null || datasets.isEmpty()) {
            LOGGER.error("Symbol {} has no available data feeds.", symbol);
            return List.of();
        } else if (datasets.size() > 1) {
            // Find the best available dataset which will be used for this and any subsequent reads
            datafeeds.put(mapKey, new ArrayList<>(Collections.singletonList(findBestHistoricalDataset(datasets, intervalDuration, intervalUnit))));
        }

        HistoricalDataset dataset = datafeeds.get(mapKey).getFirst();
        Timestamp lastSeenTime = lastSeenOffsets.get(mapKey);

        if (dataset == null) {
            return List.of();
        }

        Deque<Candlestick> prebuiltCandlesticks = candlesticksPendingReturn.get(mapKey);
        Integer expectedDuration = datafeedIntervalMap.get(mapKey);
        IntervalUnitEnum expectedUnit = dataFeedIntervalUnitMap.get(mapKey);

        List<Candlestick> dataFeedToReturn = new ArrayList<>();

        if (expectedDuration != null && expectedUnit != null && (expectedDuration != intervalDuration || expectedUnit != intervalUnit)) {
            throw new IllegalStateException("Can not modify the interval duration and units after the first read.");
        } else if (prebuiltCandlesticks != null && prebuiltCandlesticks.size() > 1) {
            // If we already have built candlesticks for this, then just grab the first one
            Candlestick first = prebuiltCandlesticks.removeFirst();
            backtesterSharedService.updateDataFeed(mapKey, first, first.getTimestamp());
            return new ArrayList<>(List.of(first));
        } else if (prebuiltCandlesticks != null && prebuiltCandlesticks.size() == 1) {
            // If we will be removing the last prebuild candlestick do not return it right away, we need to build some more.
            dataFeedToReturn.add(prebuiltCandlesticks.removeFirst());
        } else if (prebuiltCandlesticks == null) {
            // If we have not read from the datafeed yet then initialize values in the maps
            prebuiltCandlesticks = new ArrayDeque<>();
            candlesticksPendingReturn.put(mapKey, prebuiltCandlesticks);
            datafeedIntervalMap.put(mapKey, intervalDuration);
            dataFeedIntervalUnitMap.put(mapKey, intervalUnit);
        }

        // Align the first Candlestick
        Candlestick firstCandlestick = null;
        while (true) {
            List<Candlestick> candlesticks = candlestickDAO.getPaginatedCandlesticksFromHistoricalDataset(dataset, lastSeenTime, 1);
            if (candlesticks.isEmpty()) {
                break;
            }
            firstCandlestick = candlesticks.getFirst();
            lastSeenTime = firstCandlestick.getTimestamp();
            if (firstCandlestick.getTimestamp().toInstant().getEpochSecond() % ((long) intervalDuration * intervalUnit.secondsPer) == 0) {
                break;
            }
        }

        if (firstCandlestick == null) {
            lastSeenOffsets.put(mapKey, lastSeenTime);
            return dataFeedToReturn;
        }

        // Determine how many Candlesticks will be condensed into a single Candlestick
        int numCandlesToCondense = (intervalDuration * intervalUnit.secondsPer) / (dataset.getTimeInterval() * dataset.getIntervalUnit().secondsPer);

        // Determine how many Candlesticks we will pull in this run.
        int numCandlestickToQuery = MAX_CANDLESTICKS;
        while (numCandlestickToQuery % numCandlesToCondense != 0 && numCandlestickToQuery > 0) {
            numCandlestickToQuery--;
        }

        Deque<Candlestick> candlesticksToCondense = new ArrayDeque<>(candlestickDAO.getPaginatedCandlesticksFromHistoricalDataset(dataset, lastSeenTime, numCandlestickToQuery - 1));
        candlesticksToCondense.addFirst(firstCandlestick);
        lastSeenTime = candlesticksToCondense.getLast().getTimestamp();

        Object datafeedLock = datafeedLocks.computeIfAbsent(mapKey, _ -> new Object());

        synchronized (datafeedLock) {

            // Build Candlesticks of desired size
            while (candlesticksToCondense.size() >= numCandlesToCondense) {
                List<Candlestick> singleCandlestickList = new ArrayList<>();
                for (int i = 0; i < numCandlesToCondense; i++) {
                    singleCandlestickList.add(candlesticksToCondense.removeFirst());
                }

                Candlestick condensed = condenseCandlesticks(singleCandlestickList);

                prebuiltCandlesticks.add(condensed);
            }

            if (dataFeedToReturn.isEmpty() && !prebuiltCandlesticks.isEmpty()) {
                dataFeedToReturn.add(prebuiltCandlesticks.removeFirst());
            }

            lastSeenOffsets.put(mapKey, lastSeenTime);

            if(!dataFeedToReturn.isEmpty()) {
                backtesterSharedService.updateDataFeed(mapKey, dataFeedToReturn.getFirst(), dataFeedToReturn.getFirst().getTimestamp());
            }

            return dataFeedToReturn;
        }
    }

    @Override
    public void unsubscribeFromDataFeed(long threadID, Symbol symbol) {
        Symbol persistedSymbol = symbolDAO.getPersistedVersionOfSymbol(symbol);
        if (persistedSymbol == null) {
            LOGGER.error("Symbol {} is not persisted, therefore can not unsubscribe.", symbol);
            return;
        }

        BacktesterDataFeedKey mapKey = new BacktesterDataFeedKey(persistedSymbol.getSymbolID(), threadID);

        datafeeds.remove(mapKey);
        lastSeenOffsets.remove(mapKey);
        datafeedIntervalMap.remove(mapKey);
        dataFeedIntervalUnitMap.remove(mapKey);
        candlesticksPendingReturn.remove(mapKey);
    }

    @Override
    public List<ContractDetails> getContractDetailsForSymbol(Symbol symbol) {
        LOGGER.error("getContractDetailsForSymbol is not supported by the backtester.");
        return List.of();
    }

    /**
     * Takes a list of Candlesticks and combines them into one.
     * @param candlesticks List of Candlesticks.
     * @return Single Candlestick aggregating the list.
     */
    private Candlestick condenseCandlesticks(List<Candlestick> candlesticks) {
        Float open = null;
        float high = -Float.MAX_VALUE;
        float low = Float.MAX_VALUE;
        Float close = null;
        float volume = 0;
        Timestamp time = null;
        for(int i = 0; i < candlesticks.size()  ; i++) {
            Candlestick current = candlesticks.get(i);
            if(i == 0) {
                open = current.getOpen();
                time = current.getTimestamp();
            }
            if(i == candlesticks.size() - 1) {
                close = current.getClose();
            }
            volume += current.getVolume();
            low = Math.min(low, current.getLow());
            high = Math.max(high, current.getHigh());
        }

        return new Candlestick(open, high, low, close, volume, time);
    }

    /**
     * Find the HistoricalDataset that will be best for creating Candlesticks of the given duration and unit.
     * @param datasets List of HistoricalDatasets to choose from.
     * @param intervalDuration How many of the interval unit should a Candlestick represent.
     * @param intervalUnit What unit of time is the Candlestick time in.
     * @return HistoricalDataset that is best for the use case, null if none can be used.
     */
    private HistoricalDataset findBestHistoricalDataset(List<HistoricalDataset> datasets, int intervalDuration, IntervalUnitEnum intervalUnit) {
        HistoricalDataset bestFeed = null;
        IntervalUnitEnum bestFeedIntervalUnit;
        Integer bestFeedIntervalDuration;

        for(HistoricalDataset dataset : datasets) {
            IntervalUnitEnum feedIntervalUnit = dataset.getIntervalUnit();
            Integer feedIntervalDuration = dataset.getTimeInterval();

            // Do not consider feeds that are not granular enough
            if(feedIntervalUnit.compareTo(intervalUnit) > 0 || (feedIntervalUnit.equals(intervalUnit) && feedIntervalDuration > intervalDuration)) {
                continue;
            }

            // We need to be able to build the desired Candlestick size from this data
            if((intervalDuration * intervalUnit.secondsPer) % (feedIntervalUnit.secondsPer * feedIntervalDuration)  != 0) {
                continue;
            }

            // If we have an exact match for that data we want to use, use it
            if(feedIntervalDuration.equals(intervalDuration) && feedIntervalUnit.equals(intervalUnit)) {
                bestFeed = dataset;
                break;
            }

            if(bestFeed == null) {
                bestFeed = dataset;
                continue;
            }

            bestFeedIntervalUnit = bestFeed.getIntervalUnit();
            bestFeedIntervalDuration = bestFeed.getTimeInterval();
            // If we made it this far, then just take the one that covers a larger time period per candlestick since it will
            // be more efficient to test with
            if(bestFeedIntervalDuration * bestFeedIntervalUnit.secondsPer < feedIntervalDuration * feedIntervalUnit.secondsPer) {
                bestFeed = dataset;
            }
        }

        return bestFeed;
    }

}
