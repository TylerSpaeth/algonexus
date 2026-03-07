package com.github.tylerspaeth.ui.view.common;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

/**
 * Simple view for displaying text without any buttons.
 */
public class AbstractDetailView extends AbstractView {

    private String text;

    public AbstractDetailView() {}

    public AbstractDetailView(int leftPadding, int topPadding) {
        super(leftPadding, topPadding);
    }

    @Override
    public void render(Screen screen) {
        if(text == null) {
            return;
        }

        var textGraphics = screen.newTextGraphics();

        int startRow = mutableTopPadding;
        int col = mutableLeftPadding;

        for(String line : text.split("\n")) {
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                    .setBackgroundColor(TextColor.ANSI.DEFAULT)
                    .putString(col, startRow++, line);
        }
    }

    @Override
    public ViewAction handleInput(KeyStroke keyStroke) {
        if(keyStroke.getKeyType() == KeyType.Escape) {
            return ViewAction.pop();
        }
        return ViewAction.none();
    }

    /**
     * Set the text to be displayed.
     * @param text String
     */
    public void setText(String text) {
        this.text = text;
    }
}
