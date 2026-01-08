package com.github.tylerspaeth.ui.view.settings;

import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.view.AbstractMenuView;

public class SettingsMenu extends AbstractMenuView {

    @Override
    public void onEnter(UIContext uiContext)    {
        super.onEnter(uiContext);
        setTopText("Settings Menu");
    }

}
