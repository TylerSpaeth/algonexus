package com.github.tylerspaeth.ui.view.common;

import com.github.tylerspaeth.ui.UIContext;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for displaying multiple AbstractViews stacked on top of each other.
 */
public class VerticalMultiView extends AbstractMultiView {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticalMultiView.class);

    @Override
    public void render(UIContext uiContext, Screen screen) throws Exception {

        if(views == null) {
            return;
        }

        var textGraphics = screen.newTextGraphics();

        TerminalSize size = screen.getTerminalSize();

        // Size of each subview
        int viewSize = (size.getRows() - views.size() + 1) / views.size();

        for(int i = 0; i < views.size(); i++) {
            AbstractView view = views.get(i);
            view.mutableTopPadding = view.finalTopPadding + i * viewSize;

            char c = '-';
            if(i == selectedViewIndex) {
                c = 'v';
            } else if(i-1 == selectedViewIndex) {
                c =  '^';
            }

            if(i != 0) {
                int row = i*viewSize-1;
                textGraphics.drawLine(0, row, size.getColumns()-1, row, c);
            }

            view.render(uiContext, screen);
        }
    }

    @Override
    public ViewAction handleInput(UIContext uiContext, KeyStroke keyStroke) throws Exception {

        if(views == null || views.isEmpty()) {
            return ViewAction.none();
        }

        // Move between views with CTRL+UP and CTRL+DOWN
        if(keyStroke.isCtrlDown()) {
            if(keyStroke.getKeyType() == KeyType.ArrowDown) {
                selectedViewIndex = ++selectedViewIndex % views.size();
                return ViewAction.none();
            } else if(keyStroke.getKeyType() == KeyType.ArrowUp) {
                selectedViewIndex = --selectedViewIndex % views.size();
                return ViewAction.none();
            }
        }

        // Prevent the view index from pointing to an invalid view
        if(selectedViewIndex >= views.size()) {
            selectedViewIndex = 0;
        }

        AbstractView selectedView = views.get(selectedViewIndex);

        ViewAction action = selectedView.handleInput(uiContext, keyStroke);

        if(action == null || action.type == ViewAction.Type.NONE) {
            return ViewAction.none();
        }

        // Allow container-local replacement
        if(action.type == ViewAction.Type.REPLACE) {
            if(childViewChangeTakesWholeScreen == null || !childViewChangeTakesWholeScreen.get(selectedViewIndex)) {
                selectedView.onExit(uiContext);
                views.set(selectedViewIndex, action.view);
                action.view.onEnter(uiContext);
                return ViewAction.none();
            }
        }

        // If not handled, propagate
        return action;
    }
}
