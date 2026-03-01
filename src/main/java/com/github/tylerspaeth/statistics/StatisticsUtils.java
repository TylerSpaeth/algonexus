package com.github.tylerspaeth.statistics;

import com.github.tylerspaeth.common.data.dao.CandlestickDAO;
import com.github.tylerspaeth.common.data.entity.*;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.common.enums.SideEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for calculating statistics.
 */
public class StatisticsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsUtils.class);

    private static final CandlestickDAO candlestickDAO = new CandlestickDAO();

    /**
     * Calculates the PnL including fees of a collection of trades.
     * @param trades List<Trade>
     * @return PnL including fees
     */
    public static float calculatePnL(List<Trade> trades) {
        float currentPositionQuantity = 0;
        float currentPositionAveragePrice = 0;
        float realizedPnL = 0;
        float totalFees = 0;

        for(Trade trade : trades) {

            float signedFillQuantity = signedQuantity(trade.getFillQuantity(), trade.getSide());
            float fillPrice = trade.getFillPrice();

            if(currentPositionQuantity == 0) {
                // Currently no open position
                currentPositionQuantity = signedFillQuantity;
                currentPositionAveragePrice = fillPrice;
            } else if(Math.signum(currentPositionQuantity) == Math.signum(signedFillQuantity)) {
                // Currently an open position and this trade increases the size
                currentPositionAveragePrice = (currentPositionAveragePrice * Math.abs(currentPositionQuantity) + trade.getFillQuantity() * trade.getFillPrice()) / (Math.abs(currentPositionQuantity) + trade.getFillQuantity());
                currentPositionQuantity += signedFillQuantity;
            } else {
                // Currently an open position and this trade reduces it
                float quantityBeforeClose = currentPositionQuantity;

                float closeQuantity = Math.min(Math.abs(currentPositionQuantity), trade.getFillQuantity());
                realizedPnL +=  closeQuantity * (trade.getFillPrice() - currentPositionAveragePrice) * Math.signum(currentPositionQuantity);

                currentPositionQuantity += signedFillQuantity;

                if(Math.signum(currentPositionQuantity) != Math.signum(quantityBeforeClose) && currentPositionQuantity != 0) {
                    currentPositionAveragePrice = fillPrice;
                }
            }

            totalFees += trade.getFees();
        }

        // Apply the tick value if appropriate
        try {
            Symbol symbol = trades.getFirst().getOrder().getSymbol();
            realizedPnL = realizedPnL / symbol.getTickSize() * symbol.getTickValue();
            LOGGER.info("PnL calculated using tick size and value.");
        } catch (NullPointerException _) {}

        return realizedPnL - totalFees;
    }

    /**
     * Makes a quantity signed depending on the side of the trade (+Buy, -Sell)
     * @param quantity quantity of the trade.
     * @param side side of the trade.
     * @return Signed quantity.
     */
    private static float signedQuantity(float quantity, SideEnum side) {
        if(side == com.github.tylerspaeth.common.enums.SideEnum.SELL) {
            return -quantity;
        }
        return quantity;
    }

    /**
     * Calculates the number of positions that were taken in a collection of trades. A position is a duration in which
     * a nonzero quantity is held and only closes once the quantity held hits 0 or a reversal happens (ex. Going from long on a position to short).
     * @param trades List<Trade>
     * @return Number of positions that were taken.
     */
    public static int calculatePositionsTaken(List<Trade> trades) {

        int positionCount = 0;
        float currentPositionSize = 0;

        for(Trade trade : trades) {
            float signedQuantity = signedQuantity(trade.getFillQuantity(), trade.getSide());

            if(currentPositionSize == 0) {
                currentPositionSize = signedQuantity;
            } else if (Math.signum(currentPositionSize) == Math.signum(signedQuantity) || Math.abs(currentPositionSize) > Math.abs(signedQuantity)) {
                currentPositionSize += signedQuantity;
            } else {
                currentPositionSize += signedQuantity;
                positionCount++;
            }
        }

        // Any open position when the backtest ends will not be considered since we don't know what the outcome will be.

        return positionCount;
    }

    /**
     * Calculate the Sharpe Ratio for a BacktestResult based on a provided risk-free rate.
     * @param backtestResult Completed BacktestResult to calculate Sharpe Ratio on.
     * @param riskFreeRate Risk-free rate to use in Sharpe Ratio.
     * @return Sharpe Ratio
     */
    public static float calculateSharpeRatio(BacktestResult backtestResult, float riskFreeRate) {

        if(backtestResult.getStartingBalance() == null) {
            LOGGER.error("Unable to calculate a Sharpe ratio without a starting balance.");
            return 0;
        }

        if(backtestResult.getOrders().isEmpty()) {
            LOGGER.warn("No orders in backtest for Sharpe ratio calculation.");
            return 0;
        }

        Order orderWithDataset = backtestResult.getOrders().stream().filter(order -> order.getHistoricalDataset() != null).findFirst().orElse(null);
        if(orderWithDataset == null || orderWithDataset.getHistoricalDataset() == null) {
            LOGGER.error("Unable to calculate Sharpe since orders do not have linked HistoricalDataset.");
            return 0;
        }
        HistoricalDataset historicalDataset = orderWithDataset.getHistoricalDataset();

        Symbol symbol = backtestResult.getOrders().getFirst().getSymbol();
        LocalDate startDay = historicalDataset
                .getDatasetStart()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
        LocalDate endDay = historicalDataset
                .getDatasetEnd()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
        Map<LocalDate, List<Trade>> trades = backtestResult.getOrders().stream()
                .flatMap(order -> order.getTrades().stream())
                .collect(Collectors.groupingBy(t -> t.getTimestamp().toInstant().atZone((ZoneOffset.UTC)).toLocalDate()));

        float dailyRiskFreeRate = (float)(Math.pow(1 + riskFreeRate, 1.0 / 252.0) - 1);
        boolean isFuture = symbol.getAssetType() == AssetTypeEnum.FUTURES;
        float multiplier = symbol.getTickSize() == null || symbol.getTickValue() == null ? 1 :  symbol.getTickValue() / symbol.getTickSize();

        // Current cash balance
        float currentBalance = backtestResult.getStartingBalance();
        List<Float> excessReturns = new ArrayList<>();

        Float lastClosePrice = null;
        float positionQuantity = 0;

        Float previousEquity = null;

        // Calculate excess returns each day
        while(!startDay.isAfter(endDay)) {
            Candlestick lastCandlestick = candlestickDAO.getLastCandlestickBeforeTimestamp(historicalDataset, Timestamp.valueOf(startDay.plusDays(1).atStartOfDay()));
            Float closePrice = lastCandlestick != null ? lastCandlestick.getClose() : null;
            if(closePrice == null) {
                startDay = startDay.plusDays(1);
                continue;
            }

            // Futures pnl is realized as it changes
            if(lastClosePrice != null && positionQuantity != 0 && isFuture) {
                currentBalance += (closePrice - lastClosePrice) * positionQuantity * multiplier;
            }

            lastClosePrice = closePrice;

            List<Trade> todaysTrades = trades.getOrDefault(startDay, List.of());

            // Apply the trades for the day
            for(Trade trade : todaysTrades) {
                float signedQuantity = trade.getSide() == SideEnum.BUY ? trade.getFillQuantity() : -trade.getFillQuantity();
                float price = trade.getFillPrice();

                if(isFuture) {
                    float newPosition = positionQuantity + signedQuantity;

                    if (positionQuantity == 0f && newPosition != 0f) {
                        lastClosePrice = price;
                    }
                    else if (positionQuantity != 0f && newPosition == 0f) {
                        lastClosePrice = null;
                    }
                    else if (Math.signum(positionQuantity) != Math.signum(newPosition)) {
                        lastClosePrice = price;
                    }

                    positionQuantity = newPosition;
                } else {
                    currentBalance -= signedQuantity * price * multiplier;
                    positionQuantity += signedQuantity;
                }

                currentBalance -= trade.getFees();
            }

            float equity;
            if(isFuture) {
                equity = currentBalance;
            } else {
                equity = currentBalance + positionQuantity * closePrice * multiplier;
            }

            if(previousEquity != null && previousEquity != 0) {
                float dailyReturn = (equity - previousEquity) / previousEquity;
                excessReturns.add(dailyReturn - dailyRiskFreeRate);
            }

            previousEquity = equity;
            startDay = startDay.plusDays(1);

        }

        if (excessReturns.isEmpty()) {
            return 0;
        }

        float mean = calculateMean(excessReturns);
        float sd = calculateStandardDeviation(mean, excessReturns);

        if(sd == 0) {
            return 0;
        }

        return (mean / sd) * (float) Math.sqrt(252);
    }

    /**
     * Calculate the mean of a list of floats
     * @param values List of floats
     * @return Mean
     */
    private static float calculateMean(List<Float> values) {
        float mean = 0f;
        for (float r : values) {
            mean += r;
        }
        mean /= values.size();
        return mean;
    }

    /**
     * Calculate the standard deviation of a set of values from a mean.
     * @param mean Mean to calculate SD from.
     * @param values Values for SD calculation.
     * @return Standard Deviation
     */
    private static float calculateStandardDeviation(float mean, List<Float> values) {
        float variance = 0f;
        for (float r : values) {
            float diff = r - mean;
            variance += diff * diff;
        }
        variance /= values.size();

        return (float) Math.sqrt(variance);
    }

}
