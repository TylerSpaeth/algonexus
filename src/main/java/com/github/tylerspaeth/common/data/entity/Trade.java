package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.enums.SideEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "Trades")
public class Trade {

    @Id
    @Column(name = "TradeID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tradeID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", referencedColumnName = "OrderID", nullable = false)
    private Order order;

    @Column(name = "FillQuantity")
    private Float fillQuantity;

    @Column(name = "FillPrice")
    private Float fillPrice;

    @Column(name = "Side")
    @Enumerated(EnumType.STRING)
    private SideEnum side;

    @Column(name = "Fees")
    private Float fees;

    @Column(name = "ExternalTradeID")
    private String externalTradeID;

    @Column(name = "Timestamp")
    private Timestamp timestamp;

    public Integer getTradeID() {
        return tradeID;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Float getFillQuantity() {
        return fillQuantity;
    }

    public void setFillQuantity(Float fillQuantity) {
        this.fillQuantity = fillQuantity;
    }

    public Float getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(Float fillPrice) {
        this.fillPrice = fillPrice;
    }

    public SideEnum getSide() {
        return side;
    }

    public void setSide(SideEnum side) {
        this.side = side;
    }

    public Float getFees() {
        return fees;
    }

    public void setFees(Float fees) {
        this.fees = fees;
    }

    public String getExternalTradeID() {
        return externalTradeID;
    }

    public void setExternalTradeID(String externalTradeID) {
        this.externalTradeID = externalTradeID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
