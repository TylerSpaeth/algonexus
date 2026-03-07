package com.github.tylerspaeth.ui.view.common;

import com.github.tylerspaeth.ui.UIContext;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

public abstract class AbstractView {

    // Number of columns to leave blank on top and left. These are for use in concrete Views.
    // The final paddings are the relative paddings for this view.
    // The mutable paddings represent the actual position in the terminal. They should be treated as read only by concrete classes.
    protected int mutableLeftPadding;
    protected int mutableTopPadding;
    protected final int finalLeftPadding;
    protected final int finalTopPadding;

    public AbstractView() {
        this.mutableLeftPadding = 0;
        this.mutableTopPadding = 0;
        this.finalLeftPadding = 0;
        this.finalTopPadding = 0;
    }

    public AbstractView(int leftPadding, int topPadding) {
        this.mutableLeftPadding = leftPadding;
        this.mutableTopPadding = topPadding;
        this.finalLeftPadding = leftPadding;
        this.finalTopPadding  = topPadding;
    }

    /**
     * Called when the View becomes active.
     * @param uiContext UIContext with all app context that is available to the UI.
     */
    public void onEnter(UIContext uiContext) {}

    /**
     * Called when the View becomes active again after temporarily becoming inactive.
     * @param uiContext UIContext with all app context that is available to the UI.
     */
    public void onResume(UIContext uiContext) {}

    /**
     * Called when the View becomes inactive.
     * @param uiContext UIContext with all app context that is available to the UI.
     */
    public void onExit(UIContext uiContext) {}

    /**
     * Renders new changes to the inactive buffer.
     * @param uiContext UIContext for use when rendering.
     * @param screen Screen that will be rendered to.
     * @throws Exception Exception
     */
    public abstract void render(UIContext uiContext, Screen screen) throws Exception;

    /**
     * Handles user input.
     * @param uiContext UIContext for use when handling input.
     * @param keyStroke Key that was pressed.
     * @return ViewAction that resulted from the provided input.
     * @throws Exception Exception
     */
    public abstract ViewAction handleInput(UIContext uiContext, KeyStroke keyStroke) throws Exception;
}
