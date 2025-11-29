package com.github.tylerspaeth.broker.datastream;

import java.util.Objects;

public final class DataFeedKey {

    private Integer reqId;
    private final String ticker;
    private final String secType;
    private final String exchange;
    private final String currency;

    public DataFeedKey(Integer reqId, String ticker, String secType, String exchange, String currency) {
        this.reqId = reqId;
        this.ticker = ticker;
        this.secType = secType;
        this.exchange = exchange;
        this.currency = currency;
    }

    public void setReqId(Integer reqId) {
        this.reqId = reqId;
    }

    public Integer getReqId() {
        return reqId;
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass() != DataFeedKey.class) {
            return false;
        }

        DataFeedKey cast = (DataFeedKey) other;
        return cast.ticker.equals(ticker) &&
                cast.secType.equals(secType) &&
                cast.exchange.equals(exchange) &&
                cast.currency.equals(currency);
    }

    public Integer reqId() {
        return reqId;
    }

    public String ticker() {
        return ticker;
    }

    public String secType() {
        return secType;
    }

    public String exchange() {
        return exchange;
    }

    public String currency() {
        return currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reqId, ticker, secType, exchange, currency);
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


}
