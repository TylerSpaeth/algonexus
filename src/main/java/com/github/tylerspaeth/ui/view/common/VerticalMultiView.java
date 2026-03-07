package com.github.tylerspaeth.ui.view.common;

import com.github.tylerspaeth.ui.UIContext;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for displaying multiple AbstractViews stacked on top of each other.
 */
public class VerticalMultiView extends AbstractView {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerticalMultiView.class);

    private int selectedViewIndex = 0;
    private List<AbstractView> views;
    private List<Boolean> childViewChangeTakesWholeScreen;

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);
        for(AbstractView view : views) {
            view.onEnter(uiContext);
        }
    }

    @Override
    public void onExit() {
        super.onExit();
        for(AbstractView view : views) {
            view.onExit();
        }
    }

    @Override
    public void render(Screen screen) throws Exception {

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

            if(i != 0) {
                int row = i*viewSize-1;
                textGraphics.drawLine(0, row, size.getColumns()-1, row, '-');
            }

            view.render(screen);
        }
    }

    @Override
    public ViewAction handleInput(KeyStroke keyStroke) throws Exception {

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

        ViewAction action = selectedView.handleInput(keyStroke);

        if(action == null || action.type == ViewAction.Type.NONE) {
            return ViewAction.none();
        }

        // Allow container-local replacement
        if(action.type == ViewAction.Type.REPLACE) {
            if(childViewChangeTakesWholeScreen == null || !childViewChangeTakesWholeScreen.get(selectedViewIndex)) {
                selectedView.onExit();
                views.set(selectedViewIndex, action.view);
                action.view.onEnter(uiContext);
                return ViewAction.none();
            }
        }

        // If not handled, propagate
        return action;
    }

    /**
     * Sets the list of views that are displayed.
     * @param views List of AbstractView
     */
    public void setViews(List<AbstractView> views) {
        this.views = new ArrayList<>(views);
    }

    /**
     * Sets the list of views that are displayed.
     * @param views List of AbstractView
     */
    public void setViews(List<AbstractView> views, List<Boolean> childViewChangeTakesWholeScreen) {
        if(views == null || childViewChangeTakesWholeScreen == null) {
            LOGGER.error("views and childViewChangeTakesWholeScreen must be set.");
            return;
        }
        if(views.size() != childViewChangeTakesWholeScreen.size()) {
            LOGGER.error("views and childViewChangeTakesWholeScreen must be the same size.");
            return;
        }
        this.views = new ArrayList<>(views);
        this.childViewChangeTakesWholeScreen = childViewChangeTakesWholeScreen;
    }
}
