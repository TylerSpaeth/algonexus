package com.github.tylerspaeth.ui.view.livetrading;

import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.LiveTradingController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Menu with options for what to do with a given parameter set.
 */
public class ParameterSetOptionsMenu extends AbstractMenuView {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterSetOptionsMenu.class);

    private final LiveTradingController liveTradingController;

    private StrategyParameterSet strategyParameterSet;

    public ParameterSetOptionsMenu(AbstractView parent) {
        super(parent);
        liveTradingController = new LiveTradingController();
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
            LOGGER.info("Running live strategy for StrategyParameterSet {}", strategyParameterSet.getStrategyParameterSetID());
            liveTradingController.runStrategy(uiContext.engineCoordinator, uiContext.activeUser, strategyParameterSet);
            return null;
        });

        setOptions(options, optionBehaviors);
    }

    public void setStrategyParameterSet(StrategyParameterSet strategyParameterSet) {
        this.strategyParameterSet = strategyParameterSet;
    }
}