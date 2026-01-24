package com.github.tylerspaeth.ui.view.common;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractMenuView extends AbstractView {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMenuView.class);

    private String topText;
    private String bottomText;
    private List<String> options;
    private List<Supplier<AbstractView>> optionBehaviors;

    private int selected = 0;

    public AbstractMenuView(AbstractView parent) {
        super(parent);
    }

    public AbstractMenuView(AbstractView parent, int leftPadding, int topPadding)  {
        super(parent, leftPadding, topPadding);
    }


    public void setTopText(String topText) {
        this.topText = topText;
    }

    public void setBottomText(String bottomText) {
        this.bottomText = bottomText;
    }

    /**
     * Set the options and matching behaviors for the menu to display.
     * @param options String titles for each option.
     * @param optionBehaviors Suppliers that are run if the corresponding option is selected. An AbstractView will be
     *                        returned from the Supplier if the view needs to change.
     */
    public void setOptions(List<String> options, List<Supplier<AbstractView>> optionBehaviors) {
        if(options == null || optionBehaviors == null)  {
            throw new RuntimeException("Both options and option behaviors must be provided.");
        }
        if(options.size() != optionBehaviors.size()) {
            throw new RuntimeException("Options and option behaviors must both be the same length.");
        }
        this.options = options;
        this.optionBehaviors = optionBehaviors;
    }

    @Override
    public void render(Screen screen) {
        int startRow = mutableTopPadding;
        int col = mutableLeftPadding;

        TextGraphics textGraphics = screen.newTextGraphics();

        // Print top text, creating a new line when "\n" is seen
        if(topText != null) {
            String[] topTextLines = topText.split("\n");
            for(String line : topTextLines) {
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                        .setBackgroundColor(TextColor.ANSI.DEFAULT)
                        .putString(col, startRow++, line);
            }
        }

        // Print options
        if(options != null) {
            for (int i = 0; i < options.size(); i++) {
                if (i == selected) {
                    textGraphics.setForegroundColor(TextColor.ANSI.BLACK)
                            .setBackgroundColor(TextColor.ANSI.WHITE)
                            .putString(col, startRow++, "> " + options.get(i));
                } else {
                    textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                            .setBackgroundColor(TextColor.ANSI.DEFAULT)
                            .putString(col, startRow++, "  " + options.get(i));
                }
            }
        }

        // Print bottom text, creating a new line when "\n" is seen
        if(bottomText != null) {
            String[] bottomTextLines = bottomText.split("\n");
            for(String line : bottomTextLines) {
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                        .setBackgroundColor(TextColor.ANSI.DEFAULT)
                        .putString(col, startRow++, line);
            }
        }

    }

    @Override
    public AbstractView handleInput(KeyStroke keyStroke) throws Exception {
        return switch (keyStroke.getKeyType()) {
            case ArrowUp -> { selected = (selected - 1 + options.size() ) % options.size(); yield null; }
            case ArrowDown -> { selected = (selected + 1) % options.size(); yield null; }
            case Enter -> optionBehaviors.get(selected).get();
            case Escape -> parent;
            default -> null;
        };
    }

    /**
     * Get the index of the selected menu item.
     * @return Index of selected menu item.
     */
    public int getSelected() {
        return selected;
    }
}
