package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu for selecting a parameter set from a list of strategies and sending that selection to the ParameterSetOptionsMenu.
 */
public class StrategyMenu extends AbstractMenuView {

    private final Strategy strategy;

    private ParameterSetOptionsMenu parameterSetOptionsMenu;

    public StrategyMenu(AbstractView parent, Strategy strategy) {
        super(parent);
        this.strategy = strategy;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        parameterSetOptionsMenu = (ParameterSetOptionsMenu) ((HorizontalMultiView)parent).getViews().getLast();

        setTopText(strategy.toString() + "\n\nDescription: " + strategy.getDescription() + "\n\nParameter Sets:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(StrategyParameterSet strategyParameterSet : strategy.getStrategyParameterSets()) {
            options.add(strategyParameterSet.toString());
            optionBehaviors.add(() -> {
               parameterSetOptionsMenu.setStrategyParameterSet(strategyParameterSet);
               parameterSetOptionsMenu.onEnter(uiContext);
               return null;
            });
        }
        setOptions(options, optionBehaviors);
    }
}
