package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.ui.view.AbstractDetailView;
import com.github.tylerspaeth.ui.view.AbstractView;

import java.text.MessageFormat;

/**
 * See detailed view of a backtest run.
 */
public class BacktestResultsDetailView extends AbstractDetailView {

    private static final String DETAIL_VIEW_TEXT =
            """
            Start Time: {0}
            End Time: {1}
            P/L (Including fees): {2}
            Total Trade Count: {3}
            Profitable Trade Count: {4}
            Losing Trade Count: {5}
            Win/Loss Ratio: {6}
            """;

    public BacktestResultsDetailView(AbstractView parent) {
        super(parent);
    }

    public void setBacktestResult(BacktestResult backtestResult) {
        setText(MessageFormat.format(DETAIL_VIEW_TEXT,
                                     backtestResult.getStartTime(),
                                     backtestResult.getEndTime(),
                                     null,
                                     null,
                                     null,
                                     null,
                                     null));
        // TODO calculate the rest of the values
    }
}
