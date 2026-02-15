package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.BacktestController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Top level menu for backtest management.
 */
public class BacktestMenu extends AbstractMenuView {

    private final BacktestController backtestController;

    public BacktestMenu(AbstractView parent) {
        super(parent);
        this.backtestController = new BacktestController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);
        setTopText("Backtest Menu\n\nStrategies:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        List<Strategy> strategies = backtestController.getAllActiveStrategies();
        for(Strategy strategy : strategies) {
            options.add(strategy.toString());
            optionBehaviors.add(() -> {
                HorizontalMultiView horizontalMultiView = new HorizontalMultiView(this);
                StrategyMenu strategyMenu = new StrategyMenu(horizontalMultiView, strategy);
                ParameterSetOptionsMenu parameterSetOptionsMenu = new ParameterSetOptionsMenu(horizontalMultiView);
                horizontalMultiView.setViews(List.of(strategyMenu, parameterSetOptionsMenu), List.of(false, true));
                return horizontalMultiView;
            });
        }

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }

}
