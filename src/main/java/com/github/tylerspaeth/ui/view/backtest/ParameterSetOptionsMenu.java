package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;
import com.github.tylerspaeth.ui.view.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu with options for what to do with a given parameter set.
 */
public class ParameterSetOptionsMenu extends AbstractMenuView {

    private StrategyParameterSet strategyParameterSet;

    public ParameterSetOptionsMenu(AbstractView parent) {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        if(strategyParameterSet == null) {
            return;
        }

        setTopText("Select an option:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        options.add("View Backtest Results");
        optionBehaviors.add(() -> {
            HorizontalMultiView horizontalMultiView = new HorizontalMultiView(parent);
            ViewBacktestResultsMenu viewBacktestResultsMenu = new ViewBacktestResultsMenu(horizontalMultiView, strategyParameterSet);
            BacktestResultsDetailView backtestResultsDetailView = new BacktestResultsDetailView(horizontalMultiView);
            horizontalMultiView.setViews(List.of(viewBacktestResultsMenu, backtestResultsDetailView));
            return horizontalMultiView;
        });

        options.add("Run New Backtest");
        optionBehaviors.add(() -> {
            HorizontalMultiView horizontalMultiView = new HorizontalMultiView(parent);
            BacktestDatasetSelectionMenu backtestDatasetSelectionMenu = new BacktestDatasetSelectionMenu(horizontalMultiView);
            ParameterSetRunMenu parameterSetRunMenu= new ParameterSetRunMenu(horizontalMultiView, strategyParameterSet);
            horizontalMultiView.setViews(List.of(backtestDatasetSelectionMenu, parameterSetRunMenu), List.of(true, true));
            return horizontalMultiView;
        });

        setOptions(options, optionBehaviors);
    }

    public void setStrategyParameterSet(StrategyParameterSet strategyParameterSet) {
        this.strategyParameterSet = strategyParameterSet;
    }
}
