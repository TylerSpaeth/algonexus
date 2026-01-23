package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.Trade;
import com.github.tylerspaeth.ui.controller.BacktestController;
import com.github.tylerspaeth.ui.view.common.AbstractDetailView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * See detailed view of a backtest run.
 */
public class BacktestResultsDetailView extends AbstractDetailView {

    private static final String DETAIL_VIEW_TEXT =
            """
            Start Time: {0}
            End Time: {1}
            P/L (Including fees): {2}
            Individual Trade Count: {3}
            Number of Positions Taken: {4}
            """;

    private final BacktestController backtestController;

    public BacktestResultsDetailView(AbstractView parent) {
        super(parent);
        backtestController = new BacktestController();
    }

    public void setBacktestResult(BacktestResult backtestResult) {
        List<Trade> trades = new ArrayList<>();
        backtestResult.getOrders().forEach(order -> trades.addAll(order.getTrades()));
        trades.sort(Comparator.comparing(Trade::getTimestamp));
        setText(MessageFormat.format(DETAIL_VIEW_TEXT,
                                     backtestResult.getStartTime(),
                                     backtestResult.getEndTime(),
                                     backtestController.calculatePnL(trades),
                                     trades.size(),
                                     backtestController.calculatePositionsTaken(trades)));
    }

}
