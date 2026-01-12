package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.common.data.entity.StrategyParameter;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;
import com.github.tylerspaeth.ui.view.HorizontalMultiView;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu for selecting a Parameter to modify.
 */
public class ParameterSetMenu extends AbstractMenuView {

    private final StrategyParameterSet parameterSet;

    public ParameterSetMenu(AbstractView parent, StrategyParameterSet parameterSet) {
        super(parent);
        this.parameterSet = parameterSet;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText(parameterSet.toString() + "\n\nParameters:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(StrategyParameter parameter : parameterSet.getStrategyParameters()) {
            options.add(parameter.toString());
            optionBehaviors.add(() -> null);
        }

        setOptions(options, optionBehaviors);
    }

    @Override
    public AbstractView handleInput(KeyStroke keyStroke) throws Exception {

        // Override the standard menu enter logic to set the selected parameter on the update view.
        if(keyStroke.getKeyType() == KeyType.Enter) {
            ((ParameterUpdateView)((HorizontalMultiView)parent).getViews().getLast()).setSelectedParameter(parameterSet.getStrategyParameters().get(getSelected()));
            return null;
        }

        return super.handleInput(keyStroke);
    }
}
