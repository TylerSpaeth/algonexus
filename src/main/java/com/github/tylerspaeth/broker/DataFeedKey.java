package com.github.tylerspaeth.broker;

public final class DataFeedKey {

    private Integer reqId; // Set when the request is processed. Should not be set by the user.
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

    @Override
    public String toString() {
        return "DataFeedKey[" +
                "reqId=" + reqId + ", " +
                "ticker=" + ticker + ", " +
                "secType=" + secType + ", " +
                "exchange=" + exchange + ", " +
                "currency=" + currency + ']';
    }

    public void setReqId(Integer reqId) {
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
