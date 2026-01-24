package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.HistoricalDataset;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.BacktestController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu for selecting a dataset to run a backtest on.
 */
public class BacktestDatasetSelectionMenu extends AbstractMenuView {

    private final BacktestController backtestController;

    private ParameterSetRunMenu parameterSetRunMenu;

    public BacktestDatasetSelectionMenu(AbstractView parent) {
        super(parent);
        backtestController = new BacktestController();
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        parameterSetRunMenu = (ParameterSetRunMenu) ((HorizontalMultiView)parent).getViews().getLast();

        setTopText("Select a dataset:\n ");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(HistoricalDataset historicalDataset :  backtestController.getAllHistoricalDatasets()) {
            options.add(historicalDataset.toString());
            optionBehaviors.add(() -> {
                parameterSetRunMenu.onExit();
                parameterSetRunMenu.setHistoricalDataset(historicalDataset);
                parameterSetRunMenu.onEnter(uiContext);
                return null;
            });
        }

        setOptions(options, optionBehaviors);
    }
}
