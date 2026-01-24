package com.github.tylerspaeth.ui.controller;

import com.github.tylerspaeth.common.data.dao.StrategyDAO;
import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.Trade;
import com.github.tylerspaeth.common.enums.SideEnum;

import java.util.List;

/**
 * Controller for the Backtest UI elements.
 */
public class BacktestController {

    private final StrategyDAO strategyDAO;

    public BacktestController() {
        strategyDAO = new StrategyDAO();
    }

    /**
     * Retrieve all active strategies.
     * @return List of Strategy.
     */
    public List<Strategy> getAllActiveStrategies() {
        return strategyDAO.getAllActiveStrategies();
    }

    /**
     * Calculates the PnL including fees of a collection of trades.
     * @param trades List<Trade>
     * @return PnL including fees
     */
    public float calculatePnL(List<Trade> trades) {
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

        return realizedPnL - totalFees;
    }

    /**
     * Calculates the number of positions that were taken in a collection of trades. A position is a duration in which
     * a nonzero quantity is held and only closes once the quantity held hits 0 or a reversal happens (ex. Going from long on a position to short).
     * @param trades List<Trade>
     * @return Number of positions that were taken.
     */
    public int calculatePositionsTaken(List<Trade> trades) {

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
     * Makes a quantity signed depending on the side of the trade (+Buy, -Sell)
     * @param quantity quantity of the trade.
     * @param side side of the trade.
     * @return Signed quantity.
     */
    private float signedQuantity(float quantity, SideEnum side) {
        if(side == com.github.tylerspaeth.common.enums.SideEnum.SELL) {
            return -quantity;
        }
        return quantity;
    }
}
