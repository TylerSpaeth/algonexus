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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BacktesterDataFeedService implements IDataFeedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterDataFeedService.class);

    private static final Integer MAX_CANDLESTICKS = 1_000_000;

    private final SymbolDAO symbolDAO;
    private final CandlestickDAO candlestickDAO;

    private final Map<BacktesterDataFeedKey, List<HistoricalDataset>> datafeeds = new ConcurrentHashMap<>(); // Used for querying more data
    private final Map<BacktesterDataFeedKey, Timestamp> datasetTimestampCursors = new ConcurrentHashMap<>(); // Timestamp cursors for each data feed
    private final Map<BacktesterDataFeedKey, Integer> datafeedIntervalMap = new ConcurrentHashMap<>(); // Map of the interval duration being read by each data feed
    private final Map<BacktesterDataFeedKey, IntervalUnitEnum> dataFeedIntervalUnitMap = new ConcurrentHashMap<>(); // Map of the interval unit being read by each data feed
    private final Map<BacktesterDataFeedKey, Deque<Candlestick>> candlesticksPendingReturn = new ConcurrentHashMap<>(); // Map of candlesticks that have already been built for each data feed
    private final Map<BacktesterDataFeedKey, Timestamp> lastCondensedCandlestickTimestamp = new ConcurrentHashMap<>(); // Timestamps of the last candlestick that was condensed for each data feed
    private final Map<BacktesterDataFeedKey, Deque<Candlestick>> uncondensedCandlesticksPendingCondensation = new ConcurrentHashMap<>(); // Map of candlestick that have been returned from the DB but not yet condensed
    private final BacktesterSharedService backtesterSharedService;

    public BacktesterDataFeedService(BacktesterSharedService backtesterSharedService, SymbolDAO symbolDAO, CandlestickDAO candlestickDAO) {
        this.backtesterSharedService = backtesterSharedService;
        this.symbolDAO = symbolDAO;
        this.candlestickDAO = candlestickDAO;
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
        datasetTimestampCursors.put(mapKey, Timestamp.from(Instant.EPOCH));
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
        Timestamp cursor = datasetTimestampCursors.get(mapKey);

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

        long candlestickDurationInSeconds = (long) dataset.getTimeInterval() * dataset.getIntervalUnit().secondsPer;
        // Determine how many Candlesticks will be condensed into a single Candlestick
        int numCandlesToCondense = (intervalDuration * intervalUnit.secondsPer) / (int) candlestickDurationInSeconds;

        Deque<Candlestick> candlesticksToCondense = getCandlesticksForCondensation(mapKey, cursor, dataset, dataFeedToReturn.isEmpty(), (long) intervalDuration * intervalUnit.secondsPer);

        // If there are no more candlesticks to condense return whatever if in the datafeed, whether empty or not. Otherwise,
        // update the offset to whatever the last candlestick was.
        if(candlesticksToCondense.isEmpty()) {
            datasetTimestampCursors.put(mapKey, cursor);
            return dataFeedToReturn;
        } else {
            datasetTimestampCursors.put(mapKey, candlesticksToCondense.getLast().getTimestamp());
        }

        buildCandlesticksOfDesiredSize(candlesticksToCondense, numCandlesToCondense, candlestickDurationInSeconds, mapKey, prebuiltCandlesticks);

        if(!candlesticksToCondense.isEmpty()) {
            uncondensedCandlesticksPendingCondensation.put(mapKey, candlesticksToCondense);
        }

        if (dataFeedToReturn.isEmpty() && !prebuiltCandlesticks.isEmpty()) {
            dataFeedToReturn.add(prebuiltCandlesticks.removeFirst());
        }
        if(!dataFeedToReturn.isEmpty()) {
            backtesterSharedService.updateDataFeed(mapKey, dataFeedToReturn.getFirst(), dataFeedToReturn.getFirst().getTimestamp());
        }

        return dataFeedToReturn;
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
        datasetTimestampCursors.remove(mapKey);
        datafeedIntervalMap.remove(mapKey);
        dataFeedIntervalUnitMap.remove(mapKey);
        candlesticksPendingReturn.remove(mapKey);
        uncondensedCandlesticksPendingCondensation.remove(mapKey);
    }

    @Override
    public List<ContractDetails> getContractDetailsForSymbol(Symbol symbol) {
        LOGGER.error("getContractDetailsForSymbol is not supported by the backtester.");
        return List.of();
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

    /**
     * Helper function to take candlesticks from candlesticksToCondense and condense them, placing them into prebuilt candlesticks.
     * @param candlesticksToCondense Uncondensed candlesticks.
     * @param numCandlesToCondense Max number of candlesticks that should be aggregated.
     * @param candlestickDurationInSeconds Duration that one uncondensed candlestick is expected to last.
     * @param mapKey BacktesterDataFeedKey for lookups related to the data feed.
     * @param prebuiltCandlesticks Candlesticks that have been condensed and are ready for use.
     */
    private void buildCandlesticksOfDesiredSize(Deque<Candlestick> candlesticksToCondense, int numCandlesToCondense, long candlestickDurationInSeconds, BacktesterDataFeedKey mapKey, Deque<Candlestick> prebuiltCandlesticks) {

        Timestamp lastCondensedTime = lastCondensedCandlestickTimestamp.get(mapKey);

        if(lastCondensedTime == null) {
            lastCondensedTime = Timestamp.from(candlesticksToCondense.getFirst().getTimestamp().toInstant().minus(candlestickDurationInSeconds, ChronoUnit.SECONDS));
        }

        boolean endOfDataset = candlesticksToCondense.size() < numCandlesToCondense;

        // Build Candlesticks of desired size
        while ((candlesticksToCondense.size() >= numCandlesToCondense) || (endOfDataset && !candlesticksToCondense.isEmpty())) {
            List<Candlestick> singleCandlestickList = new ArrayList<>();
            int effectiveListSize = 0;
            for (int i = 0; i < numCandlesToCondense; i++) {

                Instant nextCandlestickTimeAsInstant = candlesticksToCondense.getFirst().getTimestamp().toInstant();
                long expectedIntervalInSeconds = candlestickDurationInSeconds * (i - effectiveListSize + 1);

                boolean directlyAfterPrevious = Duration.between(lastCondensedTime.toInstant().plus(effectiveListSize*expectedIntervalInSeconds, ChronoUnit.SECONDS), nextCandlestickTimeAsInstant).toSeconds() == expectedIntervalInSeconds;

                // If this candle is directly after the previous candlestick in the list
                if (directlyAfterPrevious) {
                    singleCandlestickList.add(candlesticksToCondense.removeFirst());
                    effectiveListSize = i+1;
                }
            }

            // If there are candlesticks to condense in this window, then do so
            if(!singleCandlestickList.isEmpty()) {
                Candlestick condensed = condenseCandlesticks(singleCandlestickList, numCandlesToCondense);
                prebuiltCandlesticks.add(condensed);
            }

            lastCondensedTime = Timestamp.from(lastCondensedTime.toInstant().plus(candlestickDurationInSeconds, ChronoUnit.SECONDS));
            lastCondensedCandlestickTimestamp.put(mapKey, lastCondensedTime);
        }
    }

    /**
     * Takes a list of Candlesticks and combines them into one.
     * @param candlesticks List of Candlesticks.
     * @param expectedNumberOfCandlesticks The number of candlesticks we are expecting to condense.
     * @return Single Candlestick aggregating the list.
     */
    private Candlestick condenseCandlesticks(List<Candlestick> candlesticks, int expectedNumberOfCandlesticks) {
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

        if(candlesticks.size() < expectedNumberOfCandlesticks) {
            volume = (int)(volume / candlesticks.size()) * expectedNumberOfCandlesticks;
        }

        return new Candlestick(open, high, low, close, volume, time);
    }

    /**
     * Helper for getting a selection of candlesticks to condense for reading.
     * @param mapKey BacktesterDataFeedKey for looking up values for this dataset.
     * @param lastSeenTime The time that
     * @param dataset HistoricalDataset that is being read from.
     * @param firstTimeReading Where or not this is the first time this dataset is being read from.
     * @param condensedCandlestickSizeInSeconds Size of candlesticks that this is being condensed to.
     * @return List of candlesticks that are ready to be condensed.
     */
    private Deque<Candlestick> getCandlesticksForCondensation(BacktesterDataFeedKey mapKey, Timestamp lastSeenTime, HistoricalDataset dataset, boolean firstTimeReading, long condensedCandlestickSizeInSeconds) {
        Deque<Candlestick> candlesticksToCondense = new ArrayDeque<>();

        Deque<Candlestick> uncondensedCandlesticks = uncondensedCandlesticksPendingCondensation.get(mapKey);

        // If this is the first read from this datafeed then we will align this first candlestick.
        if(firstTimeReading) {
            // Align the first Candlestick
            Candlestick firstCandlestick = null;
            while (true) {
                List<Candlestick> candlesticks = candlestickDAO.getPaginatedCandlesticksFromHistoricalDataset(dataset, lastSeenTime, 1);
                if (candlesticks.isEmpty()) {
                    break;
                }
                firstCandlestick = candlesticks.getFirst();
                lastSeenTime = firstCandlestick.getTimestamp();
                if (firstCandlestick.getTimestamp().toInstant().getEpochSecond() % condensedCandlestickSizeInSeconds == 0) {
                    break;
                }
            }

            if(firstCandlestick != null) {
                candlesticksToCondense.addFirst(firstCandlestick);
                lastSeenTime = candlesticksToCondense.getLast().getTimestamp();
            }
        } else if (uncondensedCandlesticks != null && !uncondensedCandlesticks.isEmpty()){
            // If there are leftover uncondensed candlesticks from previous retrievals then add them to the returned list.
            candlesticksToCondense.addAll(uncondensedCandlesticks);
        }

        // Query for more candlesticks
        candlesticksToCondense.addAll(candlestickDAO.getPaginatedCandlesticksFromHistoricalDataset(dataset, lastSeenTime, MAX_CANDLESTICKS));

        return candlesticksToCondense;
    }

}
