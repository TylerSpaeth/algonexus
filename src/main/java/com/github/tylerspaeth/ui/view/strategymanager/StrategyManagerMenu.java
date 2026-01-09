package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;
import com.github.tylerspaeth.ui.view.AbstractView;

public class StrategyManagerMenu extends AbstractMenuView {

    public StrategyManagerMenu(AbstractView parent) {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext)  {
        super.onEnter(uiContext);
        setTopText("Strategy Manager Menu");
    }

}
