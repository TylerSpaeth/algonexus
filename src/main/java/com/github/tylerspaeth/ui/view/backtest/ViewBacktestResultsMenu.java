package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;

// TODO

/**
 * Menu showing a list of backtest results for a parameter set with the option to select one and view the changes
 * in a BacktestResultsDetailView.
 */
public class ViewBacktestResultsMenu extends AbstractMenuView {

    private final StrategyParameterSet strategyParameterSet;

    public ViewBacktestResultsMenu(AbstractView parent, StrategyParameterSet strategyParameterSet) {
        super(parent);
        this.strategyParameterSet = strategyParameterSet;
    }
}
