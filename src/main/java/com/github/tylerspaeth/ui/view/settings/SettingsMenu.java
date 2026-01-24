package com.github.tylerspaeth.ui.view.settings;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.common.AbstractMenuView;
import com.github.tylerspaeth.ui.view.common.AbstractView;

public class SettingsMenu extends AbstractMenuView {

    public SettingsMenu(AbstractView parent)  {
        super(parent);
    }

    @Override
    public void onEnter(UIContext uiContext)    {
        super.onEnter(uiContext);
        setTopText("Settings Menu");
    }

}
