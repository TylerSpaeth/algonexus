package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.common.data.entity.StrategyParameter;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.ViewAction;
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

    private ParameterUpdateView parameterUpdateView;

    public ParameterSetMenu(StrategyParameterSet parameterSet) {
        this.parameterSet = parameterSet;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        setTopText(parameterSet.toString() + "\n\nDescription: " + parameterSet.getDescription() + "\n\nParameters:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        for(StrategyParameter parameter : parameterSet.getStrategyParameters()) {
            options.add(parameter.toString());
            optionBehaviors.add(() -> null);
        }

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }

    @Override
    public ViewAction handleInput(UIContext uiContext, KeyStroke keyStroke) throws Exception {

        // Override the standard menu enter logic to set the selected parameter on the update view.
        if(keyStroke.getKeyType() == KeyType.Enter) {
           parameterUpdateView.setSelectedParameter(parameterSet.getStrategyParameters().get(getSelected()));
            return ViewAction.none();
        }

        return super.handleInput(uiContext, keyStroke);
    }

    public void setParameterUpdateView(ParameterUpdateView parameterUpdateView) {
        this.parameterUpdateView = parameterUpdateView;
    }
}
