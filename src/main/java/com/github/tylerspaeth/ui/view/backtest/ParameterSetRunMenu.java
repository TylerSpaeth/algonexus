package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
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

    private final StrategyParameterSet strategyParameterSet;

    private HistoricalDataset historicalDataset;

    public ParameterSetRunMenu(AbstractView parent, StrategyParameterSet strategyParameterSet) {
        super(parent);
        this.strategyParameterSet = strategyParameterSet;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        if(strategyParameterSet == null || historicalDataset == null) {
            return;
        }

        setTopText("Parameter Set: " + strategyParameterSet + "\n \n" + "Dataset: " + historicalDataset);

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        options.add("Run Backtest");
        optionBehaviors.add(() -> {
            // TODO implement this
            LOGGER.info("Running backtest for StrategyParameterSet {} with Dataset {}", strategyParameterSet.getStrategyParameterSetID(), historicalDataset.getHistoricalDatasetID());
            return null;
        });

        setOptions(options, optionBehaviors);
    }

    public void setHistoricalDataset(HistoricalDataset historicalDataset) {
        this.historicalDataset = historicalDataset;
    }
}
