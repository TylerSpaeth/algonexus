package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;

public class StrategyManagerMenu extends AbstractMenuView {

    @Override
    public void onEnter(UIContext uiContext)  {
        super.onEnter(uiContext);
        setTopText("Strategy Manager Menu");
    }

}
