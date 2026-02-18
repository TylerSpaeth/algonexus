package com.github.tylerspaeth.datamanager;

import com.github.tylerspaeth.common.data.dao.HistoricalDatasetDAO;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;

@ExtendWith({MockitoExtension.class})
public class DataManagerServiceTest {

    @TempDir
    private Path tempDir;

    @Mock
    private HistoricalDatasetDAO historicalDatasetDAO;

    private DataManagerService dataManagerService;

    @BeforeEach
    public void setup() {
        dataManagerService = new DataManagerService(historicalDatasetDAO);
    }

    private HistoricalDataset buildHistoricalDataset()  {
        Exchange exchange = new Exchange();
        exchange.setName("ExchangeName");
        Symbol symbol = new Symbol();
        symbol.setExchange(exchange);
        symbol.setTicker("TCKR");
        symbol.setAssetType(AssetTypeEnum.EQUITIES);
        HistoricalDataset historicalDataset = new HistoricalDataset();
        historicalDataset.setSymbol(symbol);
        historicalDataset.setDatasetName("Test Dataset");
        historicalDataset.setDatasetSource("Test Source");
        historicalDataset.setTimeInterval(1);
        historicalDataset.setIntervalUnit(IntervalUnitEnum.MINUTE);
        historicalDataset.setDatasetStart(Timestamp.from(Instant.ofEpochSecond(0)));
        historicalDataset.setDatasetEnd(Timestamp.from(Instant.ofEpochSecond(3600)));
        historicalDataset.setLastUpdated(Timestamp.from(Instant.now()));
        return historicalDataset;
    }

    @Test
    public void testUploadWithNullDataset() {
        Path tempFile = tempDir.resolve("Test.csv");
        Assertions.assertFalse(dataManagerService.loadDatasetFromCSV(null, tempFile.toFile(), "DOCHLV", 1, new SimpleDateFormat("yyyy-MM-dd")));
    }

    @Test
    public void testUploadWithNullFile() {
        Assertions.assertFalse(dataManagerService.loadDatasetFromCSV(new HistoricalDataset(), null, "DOCHLV", 1, new SimpleDateFormat("yyyy-MM-dd")));
    }

    @Test
    public void testUploadWithNullFormat() {
        Path tempFile = tempDir.resolve("Test.csv");
        Assertions.assertFalse(dataManagerService.loadDatasetFromCSV(new HistoricalDataset(), tempFile.toFile(), null, 1, new SimpleDateFormat("yyyy-MM-dd")));
    }

    @Test
    public void testUploadWithNegativeMetadataRows() {
        Path tempFile = tempDir.resolve("Test.csv");
        Assertions.assertFalse(dataManagerService.loadDatasetFromCSV(new HistoricalDataset(), tempFile.toFile(), "DOCHLV", -1, new SimpleDateFormat("yyyy-MM-dd")));
    }

    @Test
    public void testUploadWithNullDateFormat() {
        Path tempFile = tempDir.resolve("Test.csv");
        Assertions.assertFalse(dataManagerService.loadDatasetFromCSV(new HistoricalDataset(), tempFile.toFile(), "DOCHLV", 1, null));
    }

    @Test
    public void testUploadWithEmptyStringFormat() {
        Path tempFile = tempDir.resolve("Test.csv");
        Assertions.assertFalse(dataManagerService.loadDatasetFromCSV(new HistoricalDataset(), tempFile.toFile(), "", 1, new SimpleDateFormat("yyyy-MM-dd")));
    }

    @Test
    public void testValidUploadWithOneRowWorks() throws Exception {
        Path tempFile = tempDir.resolve("Test.csv");
        Files.writeString(tempFile, "2025-10-01,1,2,3,4,5");

        HistoricalDataset historicalDataset = new HistoricalDataset();

        boolean success = dataManagerService.loadDatasetFromCSV(historicalDataset, tempFile.toFile(), "DOHLCV", 0, new SimpleDateFormat("yyyy-MM-dd"));

        Thread.sleep(100);

        Assertions.assertTrue(success);
        Assertions.assertEquals(1, historicalDataset.getCandlesticks().size());
        Candlestick candlestick = historicalDataset.getCandlesticks().getFirst();
        Assertions.assertEquals(1, candlestick.getOpen());
        Assertions.assertEquals(2, candlestick.getHigh());
        Assertions.assertEquals(3, candlestick.getLow());
        Assertions.assertEquals(4, candlestick.getClose());
        Assertions.assertEquals(5, candlestick.getVolume());
        Assertions.assertEquals(Timestamp.valueOf("2025-10-01 00:00:00"), candlestick.getTimestamp());
        Mockito.verify(historicalDatasetDAO, Mockito.times(1)).insert(Mockito.any(HistoricalDataset.class));
    }

    @Test
    public void testValidUploadWithMetadataRowWorks() throws Exception {
        Path tempFile = tempDir.resolve("Test.csv");
        Files.writeString(tempFile, "Metadata Row\n2025-10-01,1,2,3,4,5");

        HistoricalDataset historicalDataset = new HistoricalDataset();
        boolean success = dataManagerService.loadDatasetFromCSV(historicalDataset, tempFile.toFile(), "DOHLCV", 1, new SimpleDateFormat("yyyy-MM-dd"));

        Thread.sleep(100);

        Assertions.assertTrue(success);
        Assertions.assertEquals(1, historicalDataset.getCandlesticks().size());
        Candlestick candlestick = historicalDataset.getCandlesticks().getFirst();
        Assertions.assertEquals(1, candlestick.getOpen());
        Assertions.assertEquals(2, candlestick.getHigh());
        Assertions.assertEquals(3, candlestick.getLow());
        Assertions.assertEquals(4, candlestick.getClose());
        Assertions.assertEquals(5, candlestick.getVolume());
        Assertions.assertEquals(Timestamp.valueOf("2025-10-01 00:00:00"), candlestick.getTimestamp());
        Mockito.verify(historicalDatasetDAO, Mockito.times(1)).insert(Mockito.any(HistoricalDataset.class));
    }

    @Test
    public void testUploadWithFormatThatDoesNotMatch() throws Exception {
        Path tempFile = tempDir.resolve("Test.csv");
        Files.writeString(tempFile, "2025-10-01,1,2,3,4,5");
        HistoricalDataset historicalDataset = new HistoricalDataset();
        dataManagerService.loadDatasetFromCSV(historicalDataset, tempFile.toFile(), "DOHLCVXXX", 0, new SimpleDateFormat("yyyy-MM-dd"));

        Thread.sleep(100);

        Assertions.assertEquals(0, historicalDataset.getCandlesticks().size());
    }

    @Test
    public void testUploadWithNullDatasetDoesNotError() {
        Path tempFile = tempDir.resolve("Test.csv");
        Assertions.assertDoesNotThrow(() -> dataManagerService.exportDatasetToCSV(null, tempFile.toFile()));
    }

    @Test
    public void testUploadWithNullFileDoesNotError() {
        Assertions.assertDoesNotThrow(() -> dataManagerService.exportDatasetToCSV(new HistoricalDataset(), null));
    }

    @Test
    public void testUploadEmptyDatasetReturnsEmptyFile() throws Exception {
        Path tempFile = tempDir.resolve("Test.csv");
        dataManagerService.exportDatasetToCSV(new HistoricalDataset(), tempFile.toFile());

        Assertions.assertTrue(Files.readAllLines(tempFile).isEmpty());
    }

    @Test
    public void testHistoricalDatasetWithCandlesticksReturnsMetadataAndRows() throws Exception {
        Path tempFile = tempDir.resolve("Test.csv");
        HistoricalDataset historicalDataset = buildHistoricalDataset();

        Candlestick candlestick = new Candlestick();
        candlestick.setTimestamp(Timestamp.from(Instant.ofEpochSecond(0)));
        candlestick.setOpen(1f);
        candlestick.setHigh(2f);
        candlestick.setLow(3f);
        candlestick.setClose(4f);
        candlestick.setVolume(5f);
        historicalDataset.getCandlesticks().add(candlestick);

        dataManagerService.exportDatasetToCSV(historicalDataset, tempFile.toFile());

        List<String> lines = Files.readAllLines(tempFile);

        // 2 lines of metadata, 1 line of data
        Assertions.assertEquals(3, lines.size());
    }
}
