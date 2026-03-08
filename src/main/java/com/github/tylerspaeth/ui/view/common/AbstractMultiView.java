package com.github.tylerspaeth.ui.view.common;

import com.github.tylerspaeth.ui.UIContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared logic for the container MultiViews.
 */
public abstract class AbstractMultiView extends AbstractView {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMultiView.class);

    protected int selectedViewIndex = 0;
    protected List<AbstractView> views;
    protected List<Boolean> childViewChangeTakesWholeScreen;

    @Override
    public void onEnter(UIContext uiContext) {
        views.forEach(view -> view.onEnter(uiContext));
    }

    @Override
    public void onResume(UIContext uiContext) {
        views.forEach(view -> view.onResume(uiContext));
    }

    @Override
    public void onExit(UIContext uiContext) {
        views.forEach(view -> view.onExit(uiContext));
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
