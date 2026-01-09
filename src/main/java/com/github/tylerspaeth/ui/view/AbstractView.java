package com.github.tylerspaeth.ui.view;

import com.github.tylerspaeth.ui.UIContext;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

public abstract class AbstractView {

    protected final AbstractView parent;

    protected UIContext uiContext;

    // Number of columns to leave blank on top and left. These are for use in concrete Views.
    protected final int leftPadding;
    protected final int topPadding;

    public AbstractView(AbstractView parent) {
        this.parent = parent;
        this.leftPadding = 0;
        this.topPadding = 0;
    }

    public AbstractView(AbstractView parent, int leftPadding, int topPadding) {
        this.parent = parent;
        this.leftPadding = leftPadding;
        this.topPadding  = topPadding;
    }

    /**
     * Called when the View becomes active.
     * @param uiContext UIContext with all app context that is available to the UI.
     */
    public void onEnter(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    /**
     * Called when the View becomes inactive.
     */
    public void onExit() {}

    /**
     * Renders new changes to the inactive buffer.
     * @param screen Screen that will be rendered to.
     * @throws Exception Exception
     */
    public abstract void render(Screen screen) throws Exception;

    /**
     * Handles user input.
     * @param keyStroke Key that was pressed.
     * @return View that needs to be changed to, null if no change is needed.
     * @throws Exception Exception
     */
    public abstract AbstractView handleInput(KeyStroke keyStroke) throws Exception;
}
