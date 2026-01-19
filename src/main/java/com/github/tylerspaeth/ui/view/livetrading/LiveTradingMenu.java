package com.github.tylerspaeth.ui.view.livetrading;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

public class LiveTradingMenu extends AbstractMenuView {

    public LiveTradingMenu(AbstractView parent) {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext)  {
        super.onEnter(uiContext);
        setTopText("Live Trading Menu");
    }

}
