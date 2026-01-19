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
 * Container for displaying multiple AbstractViews side by side.
 */
public class HorizontalMultiView extends AbstractView {

    private static final Logger LOGGER = LoggerFactory.getLogger(HorizontalMultiView.class);

    private int selectedViewIndex = 0;
    private List<AbstractView> views;
    private List<Boolean> childViewChangeTakesWholeScreen;

    public HorizontalMultiView(AbstractView parent) {
        super(parent);
    }

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
        int viewSize = (size.getColumns() - views.size() + 1) / views.size();

        for(int i = 0; i < views.size(); i++) {
            AbstractView view = views.get(i);
            view.mutableLeftPadding = view.finalLeftPadding + i * viewSize;

            if(i != 0) {
                int column = i*viewSize-1;
                textGraphics.drawLine(column, 0, column, size.getRows()-1, '|');
            }

            view.render(screen);
        }
    }

    @Override
    public AbstractView handleInput(KeyStroke keyStroke) throws Exception {

        // Move between views with CTRL+RIGHT and CTRL+LEFT
        if(keyStroke.isCtrlDown()) {
            if(keyStroke.getKeyType() == KeyType.ArrowRight) {
                selectedViewIndex = ++selectedViewIndex % views.size();
                return null;
            } else if(keyStroke.getKeyType() == KeyType.ArrowLeft) {
                selectedViewIndex = --selectedViewIndex % views.size();
                return null;
            }
        }

        // Passthrough handleInput to the selected view if one is selected
        if(views.size() > selectedViewIndex) {
            AbstractView newView = views.get(selectedViewIndex).handleInput(keyStroke);
            if(newView == this) {
                return parent;
            }
            else if(newView != null) {
                // If the new view the child provides should take the whole screen
                if(childViewChangeTakesWholeScreen != null && childViewChangeTakesWholeScreen.get(selectedViewIndex) == true) {
                    return newView;
                }
                views.get(selectedViewIndex).onExit();
                views.set(selectedViewIndex, newView);
                views.get(selectedViewIndex).onEnter(uiContext);
            }
        } else if(selectedViewIndex != 0) {
            selectedViewIndex = 0;
        }
        return null;
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

    /**
     * Get the list of views that are displayed.
     * @return List of AbstractView
     */
    public List<AbstractView> getViews() {
        return views;
    }
}
