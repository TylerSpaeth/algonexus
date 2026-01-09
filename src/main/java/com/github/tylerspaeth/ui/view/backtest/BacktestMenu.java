package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;

public class BacktestMenu extends AbstractMenuView {

    public BacktestMenu(AbstractView parent) {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);
        setTopText("Backtest Menu");
    }

}
