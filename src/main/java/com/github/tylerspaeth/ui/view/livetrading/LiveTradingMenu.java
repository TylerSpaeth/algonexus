package com.github.tylerspaeth.ui.view.livetrading;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;

public class LiveTradingMenu extends AbstractMenuView {

    @Override
    public void onEnter(UIContext uiContext)  {
        super.onEnter(uiContext);
        setTopText("Live Trading Menu");
    }

}
