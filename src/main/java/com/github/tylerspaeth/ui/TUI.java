package com.github.tylerspaeth.ui;

import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.ViewAction;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Terminal User Interface
 */
public class TUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(TUI.class);

    private final UIContext uiContext;

    private boolean dirty = true;
    private boolean running = false;

    private final Deque<AbstractView> viewStack = new ArrayDeque<>();

    public TUI(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    /**
     * Runs the TUI.
     * @param initialView Initial view that should be displayed.
     */
    public void run(AbstractView initialView) {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();

        try (Screen screen = terminalFactory.createScreen()) {
            screen.startScreen();
            screen.setCursorPosition(null);

            viewStack.push(initialView);
            initialView.onEnter(uiContext);

            running = true;

            while (running) {

                KeyStroke keyStroke = screen.pollInput();

                if(keyStroke != null) {
                    try {

                        AbstractView currentView = viewStack.peek();

                        ViewAction action = currentView.handleInput(uiContext, keyStroke);
                        handleViewAction(action, currentView);

                    } catch (Exception e) {
                        LOGGER.error("An error occurred while handling input.", e);
                    }
                }

                // Trigger rerender when screen size changes in case the view has resizing logic
                if(screen.doResizeIfNecessary() != null) {
                    dirty = true;
                }

                // Render
                if (dirty && running) {
                    try {
                        screen.clear();
                        viewStack.peek().render(uiContext, screen);
                        screen.refresh();
                        dirty = false;

                    } catch (Exception e) {
                        LOGGER.error("An error occurred while rendering.", e);
                    }
                }

                Thread.sleep(16);
            }
        } catch (Exception e) {
            LOGGER.error("TUI crashed", e);
        }
    }

    /**
     * Handle the provided ViewAction.
     * @param action View action to handle.
     * @param currentView Currently active view.
     */
    private void handleViewAction(ViewAction action, AbstractView currentView) {
        if(action == null) {
            LOGGER.warn("Using ViewAction of type NONE instead of null is recommended.");
        } else {
            switch (action.type) {

                case PUSH -> {
                    if(action.view != null) {
                        action.view.onEnter(uiContext);
                        viewStack.push(action.view);
                    }
                }

                case POP -> {
                    currentView.onExit(uiContext);
                    viewStack.pop();
                    if(viewStack.isEmpty()) {
                        running = false;
                    } else {
                        viewStack.peek().onResume(uiContext);
                    }
                }

                case REPLACE -> {
                    viewStack.pop().onExit(uiContext);

                    action.view.onEnter(uiContext);
                    viewStack.push(action.view);
                }

                case NONE -> {}
            }
            dirty = true;
        }
    }

}
