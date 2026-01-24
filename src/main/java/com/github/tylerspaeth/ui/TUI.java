package com.github.tylerspaeth.ui;

import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminal User Interface
 */
public class TUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(TUI.class);

    private final UIContext uiContext;

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

            AbstractView currentView = initialView;
            currentView.onEnter(uiContext);

            boolean running = true;
            boolean dirty = true;

            while (running) {

                KeyStroke keyStroke = screen.pollInput();

                if(keyStroke != null) {
                    AbstractView nextView = currentView.handleInput(keyStroke);
                    if(nextView != null) {
                        currentView.onExit();
                        currentView = nextView;
                        currentView.onEnter(uiContext);
                    }
                    dirty = true;
                }

                // Trigger rerender when screen size changes in case the view has resizing logic
                if(screen.doResizeIfNecessary() != null) {
                    dirty = true;
                }

                if(dirty) {
                    screen.clear();
                    currentView.render(screen);
                    screen.refresh();
                    dirty = false;
                }

                Thread.sleep(16);
            }
        } catch (Exception e) {
            LOGGER.error("TUI crashed", e);
        }
    }

}
