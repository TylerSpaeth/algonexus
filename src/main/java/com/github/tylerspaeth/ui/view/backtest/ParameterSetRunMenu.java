package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.BacktestController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu with options for running a strategy with a given parameter set and dataset.
 */
public class ParameterSetRunMenu extends AbstractMenuView {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSetRunMenu.class);

    private final BacktestController backtestController;

    private final StrategyParameterSet strategyParameterSet;

    public ParameterSetRunMenu(AbstractView parent, StrategyParameterSet strategyParameterSet) {
        super(parent);
        this.strategyParameterSet = strategyParameterSet;
        this.backtestController = new BacktestController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        if(strategyParameterSet == null) {
            return;
        }

        setTopText("Parameter Set: " + strategyParameterSet + "\n ");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        options.add("Run Backtest");
        optionBehaviors.add(() -> {
            LOGGER.info("Running backtest for StrategyParameterSet {}", strategyParameterSet.getStrategyParameterSetID());
            backtestController.runBacktest(uiContext.engineCoordinator, uiContext.activeUser, strategyParameterSet);
            return null;
        });

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }

}
