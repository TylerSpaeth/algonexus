package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;
import com.github.tylerspaeth.ui.view.HorizontalMultiView;

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

    public ViewBacktestResultsMenu(AbstractView parent, StrategyParameterSet strategyParameterSet) {
        super(parent);
        this.strategyParameterSet = strategyParameterSet;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        backtestResultsDetailView = (BacktestResultsDetailView) ((HorizontalMultiView)parent).getViews().getLast();

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
    }
}
