package com.github.tylerspaeth.ui.view.common;

import com.github.tylerspaeth.ui.UIContext;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

public abstract class AbstractView {

    protected final AbstractView parent;

    protected UIContext uiContext;

    // Number of columns to leave blank on top and left. These are for use in concrete Views.
    // The final paddings are the relative paddings for this view.
    // The mutable paddings represent the actual position in the terminal. They should be treated as read only by concrete classes.
    protected int mutableLeftPadding;
    protected int mutableTopPadding;
    protected final int finalLeftPadding;
    protected final int finalTopPadding;

    public AbstractView(AbstractView parent) {
        this.parent = parent;
        this.mutableLeftPadding = 0;
        this.mutableTopPadding = 0;
        this.finalLeftPadding = 0;
        this.finalTopPadding = 0;
    }

    public AbstractView(AbstractView parent, int leftPadding, int topPadding) {
        this.parent = parent;
        this.mutableLeftPadding = leftPadding;
        this.mutableTopPadding = topPadding;
        this.finalLeftPadding = leftPadding;
        this.finalTopPadding  = topPadding;
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
