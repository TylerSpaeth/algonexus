package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;
import com.github.tylerspaeth.ui.view.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu for viewing the ParameterSets and details of a Strategy.
 */
public class StrategyMenu extends AbstractMenuView {

    private final Strategy strategy;

    public StrategyMenu(AbstractView parent, Strategy strategy) {
        super(parent);
        this.strategy = strategy;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText(strategy.toString() + "\n\nDescription: " + strategy.getDescription() + "\n\nParameter Sets:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(StrategyParameterSet parameterSet : strategy.getStrategyParameterSets()) {
            options.add(parameterSet.toString());
            optionBehaviors.add(() -> {
                HorizontalMultiView horizontalMultiView = new HorizontalMultiView(this);
                ParameterSetMenu parameterSetMenu = new ParameterSetMenu(horizontalMultiView, parameterSet);
                ParameterUpdateView parameterUpdateView = new ParameterUpdateView(horizontalMultiView, parameterSet);
                horizontalMultiView.setViews(List.of(parameterSetMenu, parameterUpdateView));
                return horizontalMultiView;
            });
        }

        // Option to open the form to create a new parameter set
        options.add("--CREATE NEW STRATEGY--");
        optionBehaviors.add(() -> {
           NewParameterSetForm newParameterSetForm = new NewParameterSetForm(this);
           newParameterSetForm.setStrategy(strategy);
           return newParameterSetForm;
        });

        setOptions(options, optionBehaviors);
    }

}
