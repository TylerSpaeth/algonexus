package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.StrategyManagerController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu for viewing all the available strategies.
 */
public class StrategyManagerMenu extends AbstractMenuView {

    private final StrategyManagerController strategyManagerController;

    public StrategyManagerMenu(AbstractView parent) {
        super(parent);
        this.strategyManagerController = new StrategyManagerController();
    }

    @Override
    public void onEnter(UIContext uiContext)  {
        super.onEnter(uiContext);
        setTopText("Strategy Manager Menu");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        List<Strategy> strategies = strategyManagerController.getAllActiveStrategies();
        for(Strategy strategy : strategies) {
            options.add(strategy.toString());
            optionBehaviors.add(() -> new StrategyMenu(this, strategy, strategyManagerController));
        }

        setOptions(options, optionBehaviors);
        setOptionsPerPage(10);
    }

}
