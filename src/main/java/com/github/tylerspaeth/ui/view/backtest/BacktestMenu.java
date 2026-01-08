package com.github.tylerspaeth.ui.view.backtest;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;

public class BacktestMenu extends AbstractMenuView {

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);
        setTopText("Backtest Menu");
    }

}
