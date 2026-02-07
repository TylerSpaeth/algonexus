package com.github.tylerspaeth.broker.ib.response;

public class Contract {
    private int conid;
    private String symbol;
    private String secType;
    private String lastTradeDateOrContractMonth;
    private String lastTradeDate;
    private String exchange;
    private String primaryExch;
    private String currency;

    public int getConid() {
        return conid;
    }

    public void setConid(int conid) {
        this.conid = conid;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSecType() {
        return secType;
    }

    public void setSecType(String secType) {
        this.secType = secType;
    }

    public String getLastTradeDateOrContractMonth() {
        return lastTradeDateOrContractMonth;
    }

    public void setLastTradeDateOrContractMonth(String lastTradeDateOrContractMonth) {
        this.lastTradeDateOrContractMonth = lastTradeDateOrContractMonth;
    }

    public String getLastTradeDate() {
        return lastTradeDate;
    }

    public void setLastTradeDate(String lastTradeDate) {
        this.lastTradeDate = lastTradeDate;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPrimaryExch() {
        return primaryExch;
    }

    public void setPrimaryExch(String primaryExch) {
        this.primaryExch = primaryExch;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
