package com.github.tylerspaeth.data.entity;

import com.github.tylerspaeth.enums.AssetTypeEnum;
import jakarta.persistence.*;

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

}
