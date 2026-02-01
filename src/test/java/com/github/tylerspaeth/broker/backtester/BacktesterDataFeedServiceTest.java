package com.github.tylerspaeth.broker.backtester;

import com.github.tylerspaeth.common.data.dao.*;
import com.github.tylerspaeth.common.data.entity.Candlestick;
import com.github.tylerspaeth.common.data.entity.Exchange;
import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class BacktesterDataFeedServiceTest {

    @Mock
    private SymbolDAO symbolDAO;
    @Mock
    private CandlestickDAO candlestickDAO;
    @Mock
    private OrderDAO orderDAO;
    @Mock
    private TradeDAO tradeDAO;
    @Mock
    private CommissionDAO commissionDAO;
    private BacktesterDataFeedService backtesterDataFeedService;

    @BeforeEach
    public void setup() {
        backtesterDataFeedService = new BacktesterDataFeedService(new BacktesterSharedService(orderDAO, tradeDAO, commissionDAO), symbolDAO, candlestickDAO, new ConcurrentHashMap<>());
    }

    private void mockPaginatedCandlesticks() {
        when(candlestickDAO.getPaginatedCandlesticksFromHistoricalDataset(Mockito.any(HistoricalDataset.class), Mockito.any(Timestamp.class), Mockito.anyInt())).thenAnswer(invocationOnMock ->  {
            HistoricalDataset dataset = invocationOnMock.getArgument(0);
            int numCandles = invocationOnMock.getArgument(2);
            List<Candlestick> candlesticks = new ArrayList<>();
            for(int i = 0; i < numCandles; i++) {
                if(dataset.getCandlesticks().isEmpty()) {
                    break;
                }
                candlesticks.add(dataset.getCandlesticks().removeFirst());
            }
            return candlesticks;
        });
    }

    private void setSymbolIDOnSymbol(Symbol symbol, int symbolID) throws Exception {
        Field field = symbol.getClass().getDeclaredField("symbolID");
        field.setAccessible(true);
        field.set(symbol, symbolID);
    }

    @Test
    public void testSubscribeWithNullSymbolDoesNotThrowExceptions() {
        Assertions.assertDoesNotThrow(() -> backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), null));
    }

    @Test
    public void testSubscribeWithNonPersistedSymbolDoesNotThrowExceptions() {
        Assertions.assertDoesNotThrow(() -> backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), new Symbol()));
    }

    @Test
    public void testUnsubscribeWithNullSymbolDoesNotThrowExceptions() {
        Assertions.assertDoesNotThrow(() -> backtesterDataFeedService.unsubscribeFromDataFeed(Thread.currentThread().threadId(), null));
    }

    @Test
    public void testUnsubscribeWithNonPersistedSymbolDoesNotThrowExceptions() {
        Assertions.assertDoesNotThrow(() -> backtesterDataFeedService.unsubscribeFromDataFeed(Thread.currentThread().threadId(), new Symbol()));
    }

    @Test
    public void testUnsubscribeDoesNotThrowExceptions() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);
        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        Assertions.assertDoesNotThrow(() -> backtesterDataFeedService.unsubscribeFromDataFeed(Thread.currentThread().threadId(), symbol));
    }

    @Test
    public void testReadWithoutSubscriptionReturnsEmptyList() {
        Assertions.assertTrue(backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), new Symbol(), 1, IntervalUnitEnum.SECOND).isEmpty());
    }

    @Test
    public void testReadFromNullSymbolReturnsEmptyList() {
        Assertions.assertTrue(backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), null, 2, IntervalUnitEnum.HOUR).isEmpty());
    }

    @Test
    public void testReadFromSymbolWithoutDatasetsReturnsEmptyList() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);
        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        Assertions.assertTrue(backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND).isEmpty());
    }

    @Test
    public void testReadFromSymbolWithEmptyDatasetReturnsEmptyList() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName("Test Dataset");
        historicalDataset.setSymbol(symbol);
        historicalDataset.setDatasetStart(new Timestamp(0));
        historicalDataset.setDatasetEnd(new Timestamp(1));
        historicalDataset.setTimeInterval(1);
        historicalDataset.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));

        symbol.getHistoricalDatasets().add(historicalDataset);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        Assertions.assertTrue(backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND).isEmpty());
    }

    @Test
    public void testReadSingleNotAdjustedCandlestick() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName("Test Dataset");
        historicalDataset.setSymbol(symbol);
        historicalDataset.setDatasetStart(new Timestamp(0));
        historicalDataset.setDatasetEnd(new Timestamp(1));
        historicalDataset.setTimeInterval(1);
        historicalDataset.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset);

        Candlestick candlestick = new Candlestick();
        candlestick.setTimestamp(Timestamp.from(Instant.now()));
        candlestick.setOpen(50f);
        candlestick.setClose(40f);
        candlestick.setHigh(100f);
        candlestick.setLow(10f);
        candlestick.setVolume(100000f);
        historicalDataset.getCandlesticks().add(candlestick);
        candlestick.setHistoricalDataset(historicalDataset);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(50f, returnedCandlestick.getFirst().getOpen());
        Assertions.assertEquals(40f, returnedCandlestick.getFirst().getClose());
        Assertions.assertEquals(100f, returnedCandlestick.getFirst().getHigh());
        Assertions.assertEquals(10f, returnedCandlestick.getFirst().getLow());
        Assertions.assertEquals(100000f, returnedCandlestick.getFirst().getVolume());
    }

    @Test
    public void testReadFiveCandlesticksCondensedToOne() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName("Test Dataset");
        historicalDataset.setSymbol(symbol);
        historicalDataset.setDatasetStart(new Timestamp(0));
        historicalDataset.setDatasetEnd(new Timestamp(1));
        historicalDataset.setTimeInterval(1);
        historicalDataset.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(40f);
        candlestick1.setHigh(100f);
        candlestick1.setLow(10f);
        candlestick1.setVolume(100000f);
        historicalDataset.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick2.setOpen(40f);
        candlestick2.setClose(1000f);
        candlestick2.setHigh(500f);
        candlestick2.setLow(3f);
        candlestick2.setVolume(10000f);
        historicalDataset.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset);

        Candlestick candlestick3 = new Candlestick();
        candlestick3.setTimestamp(Timestamp.from(Instant.ofEpochSecond(2)));
        candlestick3.setOpen(1000f);
        candlestick3.setClose(70f);
        candlestick3.setHigh(1100f);
        candlestick3.setLow(10f);
        candlestick3.setVolume(1000f);
        historicalDataset.getCandlesticks().add(candlestick3);
        candlestick3.setHistoricalDataset(historicalDataset);

        Candlestick candlestick4 = new Candlestick();
        candlestick4.setTimestamp(Timestamp.from(Instant.ofEpochSecond(3)));
        candlestick4.setOpen(1000f);
        candlestick4.setClose(70f);
        candlestick4.setHigh(1100f);
        candlestick4.setLow(10f);
        candlestick4.setVolume(100f);
        historicalDataset.getCandlesticks().add(candlestick4);
        candlestick4.setHistoricalDataset(historicalDataset);

        Candlestick candlestick5 = new Candlestick();
        candlestick5.setTimestamp(Timestamp.from(Instant.ofEpochSecond(4)));
        candlestick5.setOpen(1000f);
        candlestick5.setClose(70f);
        candlestick5.setHigh(1100f);
        candlestick5.setLow(10f);
        candlestick5.setVolume(10f);
        historicalDataset.getCandlesticks().add(candlestick5);
        candlestick5.setHistoricalDataset(historicalDataset);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 5, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(50f, returnedCandlestick.getFirst().getOpen());
        Assertions.assertEquals(70f, returnedCandlestick.getFirst().getClose());
        Assertions.assertEquals(1100f, returnedCandlestick.getFirst().getHigh());
        Assertions.assertEquals(3f, returnedCandlestick.getFirst().getLow());
        Assertions.assertEquals(111110f, returnedCandlestick.getFirst().getVolume());
    }

    @Test
    public void testReadThreeCandleSticksWithFirstBeingTrimmed() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName("Test Dataset");
        historicalDataset.setSymbol(symbol);
        historicalDataset.setDatasetStart(new Timestamp(0));
        historicalDataset.setDatasetEnd(new Timestamp(1));
        historicalDataset.setTimeInterval(1);
        historicalDataset.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(40f);
        candlestick1.setHigh(100f);
        candlestick1.setLow(10f);
        candlestick1.setVolume(100000f);
        historicalDataset.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(2)));
        candlestick2.setOpen(40f);
        candlestick2.setClose(1000f);
        candlestick2.setHigh(500f);
        candlestick2.setLow(3f);
        candlestick2.setVolume(10000f);
        historicalDataset.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset);

        Candlestick candlestick3 = new Candlestick();
        candlestick3.setTimestamp(Timestamp.from(Instant.ofEpochSecond(3)));
        candlestick3.setOpen(1000f);
        candlestick3.setClose(70f);
        candlestick3.setHigh(1100f);
        candlestick3.setLow(10f);
        candlestick3.setVolume(1000f);
        historicalDataset.getCandlesticks().add(candlestick3);
        candlestick3.setHistoricalDataset(historicalDataset);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 2, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(40f, returnedCandlestick.getFirst().getOpen());
        Assertions.assertEquals(70f, returnedCandlestick.getFirst().getClose());
        Assertions.assertEquals(1100f, returnedCandlestick.getFirst().getHigh());
        Assertions.assertEquals(3f, returnedCandlestick.getFirst().getLow());
        Assertions.assertEquals(11000f, returnedCandlestick.getFirst().getVolume());
    }

    @Test
    public void testReadFromSymbolWithMultipleDatasetsThatAreAllBad() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset1 = new HistoricalDataset();
        historicalDataset1.setDatasetName("Test Dataset");
        historicalDataset1.setSymbol(symbol);
        historicalDataset1.setDatasetStart(new Timestamp(0));
        historicalDataset1.setDatasetEnd(new Timestamp(1));
        historicalDataset1.setTimeInterval(3);
        historicalDataset1.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset1.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset1);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(40f);
        candlestick1.setHigh(100f);
        candlestick1.setLow(10f);
        candlestick1.setVolume(100000f);
        historicalDataset1.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset1);

        HistoricalDataset historicalDataset2 = new HistoricalDataset();
        historicalDataset2.setDatasetName("Test Dataset");
        historicalDataset2.setSymbol(symbol);
        historicalDataset2.setDatasetStart(new Timestamp(0));
        historicalDataset2.setDatasetEnd(new Timestamp(1));
        historicalDataset2.setTimeInterval(1);
        historicalDataset2.setIntervalUnit(IntervalUnitEnum.MINUTE);
        historicalDataset2.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset2);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick2.setOpen(50f);
        candlestick2.setClose(40f);
        candlestick2.setHigh(100f);
        candlestick2.setLow(10f);
        candlestick2.setVolume(100000f);
        historicalDataset2.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset2);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        Assertions.assertTrue(backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 4, IntervalUnitEnum.SECOND).isEmpty());
    }

    @Test
    public void testReadFromSymbolWithMultipleDatasetsWhereOneMatchesExactly() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset1 = new HistoricalDataset();
        historicalDataset1.setDatasetName("Test Dataset");
        historicalDataset1.setSymbol(symbol);
        historicalDataset1.setDatasetStart(new Timestamp(0));
        historicalDataset1.setDatasetEnd(new Timestamp(1));
        historicalDataset1.setTimeInterval(3);
        historicalDataset1.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset1.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset1);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(40f);
        candlestick1.setHigh(600f);
        candlestick1.setLow(20f);
        candlestick1.setVolume(100000f);
        historicalDataset1.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset1);

        HistoricalDataset historicalDataset2 = new HistoricalDataset();
        historicalDataset2.setDatasetName("Test Dataset");
        historicalDataset2.setSymbol(symbol);
        historicalDataset2.setDatasetStart(new Timestamp(0));
        historicalDataset2.setDatasetEnd(new Timestamp(60));
        historicalDataset2.setTimeInterval(1);
        historicalDataset2.setIntervalUnit(IntervalUnitEnum.MINUTE);
        historicalDataset2.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset2);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick2.setOpen(50f);
        candlestick2.setClose(20f);
        candlestick2.setHigh(100f);
        candlestick2.setLow(10f);
        candlestick2.setVolume(10000f);
        historicalDataset2.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset2);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> candlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.MINUTE);
        Assertions.assertEquals(1, candlestick.size());
        Assertions.assertEquals(50f, candlestick.getFirst().getOpen());
        Assertions.assertEquals(20f, candlestick.getFirst().getClose());
        Assertions.assertEquals(100f, candlestick.getFirst().getHigh());
        Assertions.assertEquals(10f, candlestick.getFirst().getLow());
        Assertions.assertEquals(10000f, candlestick.getFirst().getVolume());
    }

    @Test
    public void testReadFromSymbolWithMultipleDatasetsWhereOneIsAcceptable() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset1 = new HistoricalDataset();
        historicalDataset1.setDatasetName("Test Dataset");
        historicalDataset1.setSymbol(symbol);
        historicalDataset1.setDatasetStart(new Timestamp(0));
        historicalDataset1.setDatasetEnd(new Timestamp(1));
        historicalDataset1.setTimeInterval(3);
        historicalDataset1.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset1.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset1);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(40f);
        candlestick1.setHigh(600f);
        candlestick1.setLow(20f);
        candlestick1.setVolume(100000f);
        historicalDataset1.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset1);

        HistoricalDataset historicalDataset2 = new HistoricalDataset();
        historicalDataset2.setDatasetName("Test Dataset");
        historicalDataset2.setSymbol(symbol);
        historicalDataset2.setDatasetStart(new Timestamp(0));
        historicalDataset2.setDatasetEnd(new Timestamp(60));
        historicalDataset2.setTimeInterval(1);
        historicalDataset2.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset2.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset2);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick2.setOpen(50f);
        candlestick2.setClose(20f);
        candlestick2.setHigh(100f);
        candlestick2.setLow(10f);
        candlestick2.setVolume(10000f);
        historicalDataset2.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset2);

        Candlestick candlestick3 = new Candlestick();
        candlestick3.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick3.setOpen(20f);
        candlestick3.setClose(10f);
        candlestick3.setHigh(30f);
        candlestick3.setLow(10f);
        candlestick3.setVolume(1000f);
        historicalDataset2.getCandlesticks().add(candlestick3);
        candlestick3.setHistoricalDataset(historicalDataset2);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> candlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 2, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, candlestick.size());
        Assertions.assertEquals(50f, candlestick.getFirst().getOpen());
        Assertions.assertEquals(10f, candlestick.getFirst().getClose());
        Assertions.assertEquals(100f, candlestick.getFirst().getHigh());
        Assertions.assertEquals(10f, candlestick.getFirst().getLow());
        Assertions.assertEquals(11000f, candlestick.getFirst().getVolume());
    }

    @Test
    public void testReadFromSymbolWithMultipleDatasetsWhereMultipleAreAcceptable() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset1 = new HistoricalDataset();
        historicalDataset1.setDatasetName("Test Dataset");
        historicalDataset1.setSymbol(symbol);
        historicalDataset1.setDatasetStart(new Timestamp(0));
        historicalDataset1.setDatasetEnd(new Timestamp(4));
        historicalDataset1.setTimeInterval(2);
        historicalDataset1.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset1.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset1);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(20f);
        candlestick1.setHigh(100f);
        candlestick1.setLow(10f);
        candlestick1.setVolume(10000f);
        historicalDataset1.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset1);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(2)));
        candlestick2.setOpen(20f);
        candlestick2.setClose(10f);
        candlestick2.setHigh(30f);
        candlestick2.setLow(10f);
        candlestick2.setVolume(1000f);
        historicalDataset1.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset1);

        HistoricalDataset historicalDataset2 = new HistoricalDataset();
        historicalDataset2.setDatasetName("Test Dataset");
        historicalDataset2.setSymbol(symbol);
        historicalDataset2.setDatasetStart(new Timestamp(0));
        historicalDataset2.setDatasetEnd(new Timestamp(60));
        historicalDataset2.setTimeInterval(1);
        historicalDataset2.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset2.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset2);

        Candlestick candlestick3 = new Candlestick();
        candlestick3.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick3.setOpen(50f);
        candlestick3.setClose(50f);
        candlestick3.setHigh(50f);
        candlestick3.setLow(50f);
        candlestick3.setVolume(100000f);
        historicalDataset2.getCandlesticks().add(candlestick3);
        candlestick3.setHistoricalDataset(historicalDataset2);

        Candlestick candlestick4 = new Candlestick();
        candlestick4.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick4.setOpen(50f);
        candlestick4.setClose(50f);
        candlestick4.setHigh(50f);
        candlestick4.setLow(50f);
        candlestick4.setVolume(100000f);
        historicalDataset2.getCandlesticks().add(candlestick4);
        candlestick4.setHistoricalDataset(historicalDataset2);


        Candlestick candlestick5 = new Candlestick();
        candlestick5.setTimestamp(Timestamp.from(Instant.ofEpochSecond(2)));
        candlestick5.setOpen(50f);
        candlestick5.setClose(50f);
        candlestick5.setHigh(50f);
        candlestick5.setLow(50f);
        candlestick5.setVolume(10000f);
        historicalDataset2.getCandlesticks().add(candlestick5);
        candlestick5.setHistoricalDataset(historicalDataset2);

        Candlestick candlestick6 = new Candlestick();
        candlestick6.setTimestamp(Timestamp.from(Instant.ofEpochSecond(3)));
        candlestick6.setOpen(50f);
        candlestick6.setClose(50f);
        candlestick6.setHigh(50f);
        candlestick6.setLow(50f);
        candlestick6.setVolume(50f);
        historicalDataset2.getCandlesticks().add(candlestick6);
        candlestick6.setHistoricalDataset(historicalDataset2);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> candlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 4, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, candlestick.size());
        Assertions.assertEquals(50f, candlestick.getFirst().getOpen());
        Assertions.assertEquals(10f, candlestick.getFirst().getClose());
        Assertions.assertEquals(100f, candlestick.getFirst().getHigh());
        Assertions.assertEquals(10f, candlestick.getFirst().getLow());
        Assertions.assertEquals(11000f, candlestick.getFirst().getVolume());
    }

    @Test
    public void testReadMultipleCandlesticks() throws Exception {
        Symbol symbol = new Symbol();
        symbol.setName("Test Symbol");
        symbol.setTicker("TS");
        symbol.setExchange(new Exchange());
        symbol.setAssetType(AssetTypeEnum.OTHER);
        setSymbolIDOnSymbol(symbol, 1);

        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setDatasetName("Test Dataset");
        historicalDataset.setSymbol(symbol);
        historicalDataset.setDatasetStart(new Timestamp(0));
        historicalDataset.setDatasetEnd(new Timestamp(1));
        historicalDataset.setTimeInterval(1);
        historicalDataset.setIntervalUnit(IntervalUnitEnum.SECOND);
        historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));
        symbol.getHistoricalDatasets().add(historicalDataset);

        Candlestick candlestick1 = new Candlestick();
        candlestick1.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick1.setOpen(50f);
        candlestick1.setClose(40f);
        candlestick1.setHigh(100f);
        candlestick1.setLow(10f);
        candlestick1.setVolume(100000f);
        historicalDataset.getCandlesticks().add(candlestick1);
        candlestick1.setHistoricalDataset(historicalDataset);

        Candlestick candlestick2 = new Candlestick();
        candlestick2.setTimestamp(Timestamp.from(Instant.ofEpochSecond(1)));
        candlestick2.setOpen(40f);
        candlestick2.setClose(1000f);
        candlestick2.setHigh(500f);
        candlestick2.setLow(3f);
        candlestick2.setVolume(10000f);
        historicalDataset.getCandlesticks().add(candlestick2);
        candlestick2.setHistoricalDataset(historicalDataset);

        Candlestick candlestick3 = new Candlestick();
        candlestick3.setTimestamp(Timestamp.from(Instant.ofEpochSecond(2)));
        candlestick3.setOpen(1000f);
        candlestick3.setClose(70f);
        candlestick3.setHigh(1100f);
        candlestick3.setLow(10f);
        candlestick3.setVolume(1000f);
        historicalDataset.getCandlesticks().add(candlestick3);
        candlestick3.setHistoricalDataset(historicalDataset);

        Candlestick candlestick4 = new Candlestick();
        candlestick4.setTimestamp(Timestamp.from(Instant.ofEpochSecond(3)));
        candlestick4.setOpen(1000f);
        candlestick4.setClose(70f);
        candlestick4.setHigh(1100f);
        candlestick4.setLow(10f);
        candlestick4.setVolume(100f);
        historicalDataset.getCandlesticks().add(candlestick4);
        candlestick4.setHistoricalDataset(historicalDataset);

        Candlestick candlestick5 = new Candlestick();
        candlestick5.setTimestamp(Timestamp.from(Instant.ofEpochSecond(4)));
        candlestick5.setOpen(1000f);
        candlestick5.setClose(70f);
        candlestick5.setHigh(1100f);
        candlestick5.setLow(10f);
        candlestick5.setVolume(10f);
        historicalDataset.getCandlesticks().add(candlestick5);
        candlestick5.setHistoricalDataset(historicalDataset);

        when(symbolDAO.getPersistedVersionOfSymbol(Mockito.any(Symbol.class))).thenReturn(symbol);
        mockPaginatedCandlesticks();
        backtesterDataFeedService.subscribeToDataFeed(Thread.currentThread().threadId(), symbol);
        List<Candlestick> returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(Timestamp.from(Instant.ofEpochSecond(0)), returnedCandlestick.getFirst().getTimestamp());
        returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(Timestamp.from(Instant.ofEpochSecond(1)), returnedCandlestick.getFirst().getTimestamp());
        returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(Timestamp.from(Instant.ofEpochSecond(2)), returnedCandlestick.getFirst().getTimestamp());
        returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(Timestamp.from(Instant.ofEpochSecond(3)), returnedCandlestick.getFirst().getTimestamp());
        returnedCandlestick = backtesterDataFeedService.readFromDataFeed(Thread.currentThread().threadId(), symbol, 1, IntervalUnitEnum.SECOND);
        Assertions.assertEquals(1, returnedCandlestick.size());
        Assertions.assertEquals(Timestamp.from(Instant.ofEpochSecond(4)), returnedCandlestick.getFirst().getTimestamp());
    }

}
