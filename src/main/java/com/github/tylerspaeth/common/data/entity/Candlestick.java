package com.github.tylerspaeth.common.data.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "candlesticks")
public class Candlestick {

    @Id
    @Column(name = "CandlestickID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer candlestickID;

    @Column(name = "Open")
    private Float open;

    @Column(name = "Close")
    private Float close;

    @Column(name = "High")
    private Float high;

    @Column(name = "Low")
    private Float low;

    @Column(name = "Volume")
    private Float volume;

    @Column(name = "Timestamp")
    private Timestamp timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HistoricalDatasetID", referencedColumnName = "HistoricalDatasetID", nullable = false)
    private HistoricalDataset historicalDataset;

    public Integer getCandlestickID() {
        return candlestickID;
    }

    public Float getOpen() {
        return open;
    }

    public void setOpen(Float open) {
        this.open = open;
    }

    public Float getClose() {
        return close;
    }

    public void setClose(Float close) {
        this.close = close;
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }

    public Float getLow() {
        return low;
    }

    public void setLow(Float low) {
        this.low = low;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public HistoricalDataset getHistoricalDataset() {
        return historicalDataset;
    }

    public void setHistoricalDataset(HistoricalDataset historicalDataset) {
        this.historicalDataset = historicalDataset;
    }

    public String getCSVString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return MessageFormat.format("{0},{1},{2},{3},{4},{5}", formatter.format(timestamp), open, close, high, low, volume);
    }
}
