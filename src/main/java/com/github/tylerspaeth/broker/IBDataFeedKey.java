package com.github.tylerspaeth.broker;

import java.util.Objects;

public final class IBDataFeedKey {

    private Integer reqId; // Set when the request is processed. Should not be set by the user.
    private final String ticker;
    private final String secType;
    private final String exchange;
    private final String currency;

    public IBDataFeedKey(Integer reqId, String ticker, String secType, String exchange, String currency) {
        this.reqId = reqId;
        this.ticker = ticker;
        this.secType = secType;
        this.exchange = exchange;
        this.currency = currency;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || other.getClass() != IBDataFeedKey.class) {
            return false;
        }

        IBDataFeedKey cast = (IBDataFeedKey) other;
        return cast.ticker.equals(ticker) &&
                cast.secType.equals(secType) &&
                cast.exchange.equals(exchange) &&
                cast.currency.equals(currency);
    }

    @Override
    public String toString() {
        return "DataFeedKey[" +
                "reqId=" + reqId + ", " +
                "ticker=" + ticker + ", " +
                "secType=" + secType + ", " +
                "exchange=" + exchange + ", " +
                "currency=" + currency + ']';
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, secType, exchange, currency);
    }

    public void setReqId(Integer reqId) {
        if(this.reqId != null) {
            throw new IllegalStateException("reqId is already set.");
        }
        this.reqId = reqId;
    }

    public Integer getReqId() {
        return reqId;
    }

    public String getTicker() {
        return ticker;
    }

    public String getSecType() {
        return secType;
    }

    public String getExchange() {
        return exchange;
    }

    public String getCurrency() {
        return currency;
    }

}
