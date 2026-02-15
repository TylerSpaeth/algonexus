package com.github.tylerspaeth.ui.view.livetrading;

import com.github.tylerspaeth.common.data.entity.Strategy;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.LiveTradingController;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.HorizontalMultiView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Top level menu for live trading management.
 */
public class LiveTradingMenu extends AbstractMenuView {

    private final LiveTradingController liveTradingController;

    public LiveTradingMenu(AbstractView parent) {
        super(parent);
        this.liveTradingController = new LiveTradingController();
    }

    @Override
    public void onEnter(UIContext uiContext)  {
        super.onEnter(uiContext);
        setTopText("Live Trading Menu\n\nStrategies:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        List<Strategy> strategies = liveTradingController.getAllActiveStrategies();
        for(Strategy strategy : strategies) {
            options.add(strategy.toString());
            optionBehaviors.add(() -> {
                HorizontalMultiView horizontalMultiView = new HorizontalMultiView(this);
                StrategyMenu strategyMenu = new StrategyMenu(horizontalMultiView, strategy);
                ParameterSetOptionsMenu parameterSetOptionsMenu = new ParameterSetOptionsMenu(horizontalMultiView);
                horizontalMultiView.setViews(List.of(strategyMenu, parameterSetOptionsMenu), List.of(false, true));
                return horizontalMultiView;
            });

            setOptions(options, optionBehaviors);
            setOptionsPerPage(10);
        }
    }

}
