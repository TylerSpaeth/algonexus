package com.github.tylerspaeth.ui.view.livetrading;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

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

        options.add("Run Strategy");
        optionBehaviors.add(() -> {
            System.out.println("Run strategy");
            // TODO
            return null;
        });

        setOptions(options, optionBehaviors);
    }

    public void setStrategyParameterSet(StrategyParameterSet strategyParameterSet) {
        this.strategyParameterSet = strategyParameterSet;
    }
}