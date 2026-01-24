package com.github.tylerspaeth.ui.view.strategymanager;

import com.github.tylerspaeth.common.data.entity.StrategyParameter;
import com.github.tylerspaeth.common.data.entity.StrategyParameterSet;
import com.github.tylerspaeth.ui.UIContext;
import com.github.tylerspaeth.ui.controller.ParameterSetController;
import com.github.tylerspaeth.ui.view.common.AbstractView;
import com.github.tylerspaeth.ui.view.common.HorizontalMultiView;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View for updating a Parameter.
 */
public class ParameterUpdateView extends AbstractView {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterUpdateView.class);

    private final ParameterSetController parameterSetController = new ParameterSetController();

    private StrategyParameterSet parameterSet;
    private StrategyParameter selectedParameter;

    public ParameterUpdateView(AbstractView parent, StrategyParameterSet parameterSet) {
        super(parent);
        this.parameterSet = parameterSet;
    }

    @Override
    public void onEnter(UIContext uiContext) {
        super.onEnter(uiContext);
    }

    @Override
    public void render(Screen screen) {
        TextGraphics textGraphics = screen.newTextGraphics();

        if(selectedParameter != null) {

            if(selectedParameter.getValue() == null) {
                selectedParameter.setValue("");
            }

            String value = "Value: " + selectedParameter.getValue();

            textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                    .setBackgroundColor(TextColor.ANSI.DEFAULT)
                    .putString(mutableLeftPadding, mutableTopPadding, "Name: " + selectedParameter.getName());
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                    .setBackgroundColor(TextColor.ANSI.DEFAULT)
                    .putString(mutableLeftPadding, mutableTopPadding+1, "Value: " + selectedParameter.getValue());

            // Cursor
            textGraphics.setForegroundColor(TextColor.ANSI.BLACK)
                    .setBackgroundColor(TextColor.ANSI.WHITE)
                    .putString(mutableLeftPadding + value.length(), mutableTopPadding + 1, " ");
        }

    }

    @Override
    public AbstractView handleInput(KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Enter -> {
                if(selectedParameter != null) {
                    parameterSetController.updateStrategyParameter(selectedParameter);
                    // Call on enter to update the menu
                    ((HorizontalMultiView)parent).getViews().getFirst().onEnter(uiContext);
                }
            }
            case Backspace -> {
                if(selectedParameter != null && !selectedParameter.getValue().isEmpty()) {
                    selectedParameter.setValue(selectedParameter.getValue().substring(0, selectedParameter.getValue().length()-1));
                }
            }
            case Character -> {
                selectedParameter.setValue(selectedParameter.getValue() + keyStroke.getCharacter());
            }
        }
        return null;
    }

    public void setSelectedParameter(StrategyParameter selectedParameter) {
        if(selectedParameter != null && parameterSet != null && parameterSet.getStrategyParameters().contains(selectedParameter)) {
            this.selectedParameter = selectedParameter;
        } else {
            LOGGER.error("Failed to setSelectedParameter as it is not part of the current parameter set.");
        }
    }
}
