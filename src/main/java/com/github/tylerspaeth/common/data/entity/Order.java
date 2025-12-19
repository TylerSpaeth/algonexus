package com.github.tylerspaeth.common.data.entity;

import com.github.tylerspaeth.common.enums.OrderStatusEnum;
import com.github.tylerspaeth.common.enums.OrderTypeEnum;
import com.github.tylerspaeth.common.enums.SideEnum;
import com.github.tylerspaeth.common.enums.TimeInForceEnum;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Version
    @Column(name = "Version")
    private int version;

    @Id
    @Column(name = "OrderID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SymbolID", referencedColumnName = "SymbolID", nullable = false)
    private Symbol symbol;

    @Column(name = "OrderType")
    @Enumerated(EnumType.STRING)
    private OrderTypeEnum orderType;

    @Column(name = "Side")
    @Enumerated(EnumType.STRING)
    private SideEnum side;

    @Column(name = "Quantity")
    private Float quantity;

    @Column(name = "Price")
    private Float price;

    @Column(name = "TimeInForce")
    @Enumerated(EnumType.STRING)
    private TimeInForceEnum timeInForce;

    @Column(name = "TimePlaced")
    private Timestamp timePlaced;

    @Column(name = "TimeClosed")
    private Timestamp timeClosed;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private OrderStatusEnum status;

    @Column(name = "ExternalOrderID")
    private String externalOrderID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BacktestResultID", referencedColumnName = "BacktestResultID")
    private BacktestResult backtestResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StrategyParameterSetID", referencedColumnName = "StrategyParameterSetID", nullable = false)
    private StrategyParameterSet strategyParameterSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Trade> trades;

    @Column(name = "OCAGroup")
    private String OCAGroup;

    @Column(name = "LastInOCAGroup")
    private Boolean lastInOCAGroup;

    @Column(name = "TrailAmount")
    private Float trailAmount;

    @Column(name = "TrailPercent")
    private Float trailPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentOrderID", referencedColumnName = "OrderID")
    private Order parentOrder;

    @Column(name = "Finalized")
    private boolean finalized;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderEvent> orderEvents;

    public Integer getOrderID() {
        return orderID;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public OrderTypeEnum getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderTypeEnum orderType) {
        this.orderType = orderType;
    }

    public SideEnum getSide() {
        return side;
    }

    public void setSide(SideEnum side) {
        this.side = side;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public TimeInForceEnum getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(TimeInForceEnum timeInForce) {
        this.timeInForce = timeInForce;
    }

    public Timestamp getTimePlaced() {
        return timePlaced;
    }

    public void setTimePlaced(Timestamp timePlaced) {
        this.timePlaced = timePlaced;
    }

    public Timestamp getTimeClosed() {
        return timeClosed;
    }

    public void setTimeClosed(Timestamp timeClosed) {
        this.timeClosed = timeClosed;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    public String getExternalOrderID() {
        return externalOrderID;
    }

    public void setExternalOrderID(String externalOrderID) {
        this.externalOrderID = externalOrderID;
    }

    public BacktestResult getBacktestResult() {
        return backtestResult;
    }

    public void setBacktestResult(BacktestResult backtestResult) {
        this.backtestResult = backtestResult;
    }

    public StrategyParameterSet getStrategyParameterSet() {
        return strategyParameterSet;
    }

    public void setStrategyParameterSet(StrategyParameterSet strategyParameterSet) {
        this.strategyParameterSet = strategyParameterSet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOCAGroup() {
        return OCAGroup;
    }

    public void setOCAGroup(String OCAGroup) {
        this.OCAGroup = OCAGroup;
    }

    public Boolean getLastInOCAGroup() {
        return lastInOCAGroup;
    }

    public void setLastInOCAGroup(Boolean lastInOCAGroup) {
        this.lastInOCAGroup = lastInOCAGroup;
    }

    public Float getTrailAmount() {
        return trailAmount;
    }

    public void setTrailAmount(Float trailAmount) {
        this.trailAmount = trailAmount;
    }

    public Float getTrailPercent() {
        return trailPercent;
    }

    public void setTrailPercent(Float trailPercent) {
        this.trailPercent = trailPercent;
    }

    public Order getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(Order parentOrder) {
        this.parentOrder = parentOrder;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public List<OrderEvent> getOrderEvents() {
        return orderEvents;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public void setTrades(List<Trade> trades) {
        this.trades = trades;
    }
}
