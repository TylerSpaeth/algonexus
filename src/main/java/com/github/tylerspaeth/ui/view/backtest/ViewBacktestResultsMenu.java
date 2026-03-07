package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu showing a list of backtest results for a parameter set with the option to select one and view the changes
 * in a BacktestResultsDetailView.
 */
public class ViewBacktestResultsMenu extends AbstractMenuView {

    private final StrategyParameterSet strategyParameterSet;

    private BacktestResultsDetailView backtestResultsDetailView;

    public ViewBacktestResultsMenu(StrategyParameterSet strategyParameterSet, BacktestResultsDetailView backtestResultsDetailView) {
        this.strategyParameterSet = strategyParameterSet;
        this.backtestResultsDetailView = backtestResultsDetailView;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText("Select a backtest result to view:\n ");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(BacktestResult backtestResult : strategyParameterSet.getBacktestResults()) {
            options.add(backtestResult.toString());
            optionBehaviors.add(() -> {
                backtestResultsDetailView.setBacktestResult(backtestResult);
                return null;
            });
        }

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }
}
