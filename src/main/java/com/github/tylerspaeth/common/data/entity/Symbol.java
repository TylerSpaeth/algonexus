package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "symbols")
public class Symbol {

    @Id
    @Column(name = "SymbolID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer symbolID;

    @Column(name = "Ticker")
    private String ticker;

    @Column(name = "Name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExchangeID", referencedColumnName = "ExchangeID", nullable = false)
    private Exchange exchange;

    @Column(name = "AssetType")
    @Enumerated(EnumType.STRING)
    private AssetTypeEnum assetType;

    @Column(name = "Currency", length = 3)
    private String currency;

    @Column(name = "TickSize")
    private Float tickSize;

    @Column(name = "TickValue")
    private Float tickValue;

    @OneToMany(mappedBy = "symbol", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<HistoricalDataset> historicalDatasets;

    @OneToOne(mappedBy = "symbol", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Commission commission;

    @Override
    public String toString() {
        return ticker;
    }

    public Integer getSymbolID() {
        return symbolID;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public AssetTypeEnum getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetTypeEnum assetType) {
        this.assetType = assetType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Float getTickSize() {
        return tickSize;
    }

    public void setTickSize(Float tickSize) {
        this.tickSize = tickSize;
    }

    public Float getTickValue() {
        return tickValue;
    }

    public void setTickValue(Float tickValue) {
        this.tickValue = tickValue;
    }

    public List<HistoricalDataset> getHistoricalDatasets() {
        if(historicalDatasets == null) {
            historicalDatasets = new ArrayList<>();
        }
        return historicalDatasets;
    }

    public Commission getCommission() {
        return commission;
    }

    public void setCommission(Commission commission) {
        this.commission = commission;
    }
}
