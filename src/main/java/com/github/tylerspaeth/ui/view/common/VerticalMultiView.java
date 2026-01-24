package com.github.tylerspaeth.ui.view.common;

import com.github.tylerspaeth.ui.UIContext;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for displaying multiple AbstractViews stacked on top of each other.
 */
public class VerticalMultiView extends AbstractView {

    private int selectedViewIndex = 0;
    private List<AbstractView> views;

    public VerticalMultiView(AbstractView parent) {
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
    public AbstractView handleInput(KeyStroke keyStroke) throws Exception {

        // Move between views with CTRL+UP and CTRL+DOWN
        if(keyStroke.isCtrlDown()) {
            if(keyStroke.getKeyType() == KeyType.ArrowDown) {
                selectedViewIndex = ++selectedViewIndex % views.size();
                return null;
            } else if(keyStroke.getKeyType() == KeyType.ArrowUp) {
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
}
