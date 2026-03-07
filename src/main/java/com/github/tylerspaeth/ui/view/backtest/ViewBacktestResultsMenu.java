package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.BacktestResult;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.BacktestController;
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

    private final int strategyParameterSetID;

    private final BacktestResultsDetailView backtestResultsDetailView;

    private final BacktestController backtestController;

    public ViewBacktestResultsMenu(StrategyParameterSet strategyParameterSet, BacktestResultsDetailView backtestResultsDetailView) {
        this.strategyParameterSetID = strategyParameterSet.getStrategyParameterSetID();
        this.backtestResultsDetailView = backtestResultsDetailView;
        this.backtestController = new BacktestController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        setTopText("Select a backtest result to view:\n ");

        loadOptions();
    }

    @Override
    public void onRefresh(UIContext uiContext) {
        loadOptions();
    }

    /**
     * Load the menu options.
     */
    private void loadOptions() {
        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(BacktestResult backtestResult : backtestController.getBacktestResultsForStrategyParameterSetID(strategyParameterSetID)) {
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
