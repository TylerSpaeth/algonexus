package com.github.tylerspaeth.ui.view;

import com.github.tylerspaeth.common.enums.AccountTypeEnum;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.backtest.BacktestMenu;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.data.DataManagerMenu;
import com.github.tylerspaeth.ui.view.livetrading.LiveTradingMenu;
import com.github.tylerspaeth.ui.view.settings.SettingsMenu;
import com.github.tylerspaeth.ui.view.signin.SignInMenu;
import com.github.tylerspaeth.ui.view.strategymanager.StrategyManagerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MainMenuView extends AbstractMenuView {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainMenuView.class);

    public MainMenuView(AbstractView parent) {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);

        setTopText("Main Menu:");

        List<String> options = new ArrayList<>();
        List<Supplier<AbstractView>> optionBehaviors = new ArrayList<>();

        if(uiContext.activeUser.getAccountType() == AccountTypeEnum.INTERNAL) {
            options.add("Backtesting");
            optionBehaviors.add(() -> new BacktestMenu(this));
            options.add("Data Manager");
            optionBehaviors.add(() -> new DataManagerMenu(this));
        } else {
            options.add("Live Trading");
            optionBehaviors.add(() -> new LiveTradingMenu(this));
        }

        options.add("Strategy Manager");
        optionBehaviors.add(() -> new StrategyManagerMenu(this));

        options.add("Settings");
        optionBehaviors.add(() -> new SettingsMenu(this));

        options.add("Sign Out");
        optionBehaviors.add(() -> {
            LOGGER.info("Signing out user {}", uiContext.activeUser);
            return new SignInMenu(null);
        });

        options.add("Exit");
        optionBehaviors.add(() -> {
            // TODO
            return null;
        });

        setOptions(options, optionBehaviors);
    }

}
