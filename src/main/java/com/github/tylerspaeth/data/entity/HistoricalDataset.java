package com.github.tylerspaeth.data.entity;

import com.github.tylerspaeth.enums.IntervalUnitEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "historicaldataset")
public class HistoricalDataset {

    @Id
    @Column(name = "HistoricalDatasetId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historicalDatasetID;

    @Column(name = "DatasetName")
    private String datasetName;

    @Column(name = "DatasetSource")
    private String datasetSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SymbolID", referencedColumnName = "SymbolID", nullable = false)
    private Symbol symbol;

    @Column(name = "TimeInterval")
    private Integer timeInterval;

    @Column(name = "IntervalUnit")
    @Enumerated(EnumType.STRING)
    private IntervalUnitEnum intervalUnit;

    @Column(name = "DatasetStart")
    private Timestamp datasetStart;

    @Column(name = "DatasetEnd")
    private Timestamp datasetEnd;

    @Column(name = "LastUpdated")
    private Timestamp lastUpdated;

    @OneToMany(mappedBy = "historicalDataset", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Candlestick> candlesticks;

    @Override
    public String toString() {
        return datasetName;
    }

    public Integer getHistoricalDatasetID() {
        return historicalDatasetID;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDatasetSource() {
        return datasetSource;
    }

    public void setDatasetSource(String datasetSource) {
        this.datasetSource = datasetSource;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Integer getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Integer timeInterval) {
        this.timeInterval = timeInterval;
    }

    public IntervalUnitEnum getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(IntervalUnitEnum intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Timestamp getDatasetStart() {
        return datasetStart;
    }

    public void setDatasetStart(Timestamp datasetStart) {
        this.datasetStart = datasetStart;
    }

    public Timestamp getDatasetEnd() {
        return datasetEnd;
    }

    public void setDatasetEnd(Timestamp datasetEnd) {
        this.datasetEnd = datasetEnd;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    public List<Candlestick> getCandlesticks() {
        return candlesticks;
    }

    public void setCandlesticks(List<Candlestick> candlesticks) {
        this.candlesticks = candlesticks;
    }
}
