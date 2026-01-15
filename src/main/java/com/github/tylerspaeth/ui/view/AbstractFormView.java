package com.github.tylerspaeth.ui.view;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;
import java.util.function.Function;

/**
 * Form that can be filled out and submitted.
 */
public class AbstractFormView extends AbstractView {

    private Function<List<String>, AbstractView> submissionCallback;
    private List<String> formFieldLabels;
    private List<String> formFields;
    private int selected = 0;

    private String topText;
    private String bottomText;

    public AbstractFormView(AbstractView parent) {
        super(parent);
    }

    public AbstractFormView(AbstractView parent, int leftPadding, int topPadding) {
        super(parent, leftPadding, topPadding);
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

        // Print fields
        if(formFields != null) {
            for (int i = 0; i < formFields.size(); i++) {
                String lineText = formFieldLabels.get(i) + ": " + formFields.get(i);
                textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                        .setBackgroundColor(TextColor.ANSI.DEFAULT)
                        .putString(col, startRow++, lineText);
                if(i == selected) {
                    textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT)
                            .setBackgroundColor(TextColor.ANSI.WHITE)
                            .putString(col+lineText.length(), startRow-1, " ");
                }
            }
        }

        if(selected == formFields.size()) {
            textGraphics.setForegroundColor(TextColor.ANSI.BLACK)
                    .setBackgroundColor(TextColor.ANSI.WHITE)
                    .putString(col, startRow++, "Submit");
        } else {
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE)
                    .setBackgroundColor(TextColor.ANSI.DEFAULT)
                    .putString(col, startRow++, "Submit");
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
        if(keyStroke.getKeyType() == KeyType.Escape) {
            return parent;
        } else if(keyStroke.getKeyType() == KeyType.Enter && selected == formFields.size()) {
            if(submissionCallback == null) {
                return null;
            }
            return submissionCallback.apply(formFields);
        } else if(keyStroke.getKeyType() == KeyType.ArrowUp) {
            selected = (selected + formFields.size()) % (formFields.size() + 1);
        } else if(keyStroke.getKeyType() == KeyType.ArrowDown) {
            selected = (selected + 1) % (formFields.size() + 1);
        } else if(selected != formFields.size()) {
            String selectedString = formFields.get(selected);
            if(keyStroke.getKeyType() == KeyType.Backspace) {
                if(!selectedString.isEmpty()) {
                    formFields.set(selected, selectedString.substring(0, selectedString.length() - 1));
                }
            } else if(keyStroke.getCharacter() != null) {
                formFields.set(selected, selectedString + keyStroke.getCharacter());
            }
        }
        return null;
    }

    /**
     * Set callback to run when the form is submitted.
     * @param submissionCallback Function that takes the list of formFields and returns the AbstractView that should
     *                           be loaded after the form is submitted.
     */
    public void setSubmissionCallback(Function<List<String>, AbstractView> submissionCallback) {
        this.submissionCallback = submissionCallback;
    }

    /**
     * Sets the formFields and their respective labels. There must be an equal number of fields and labels.
     * @param formFieldLabels List of Strings.
     * @param formFields List of String. These can be left as empty strings or prepopulated values can be provided.
     */
    public void setFormFields(List<String> formFieldLabels, List<String> formFields) {
        if(formFieldLabels == null || formFields == null) {
            throw new RuntimeException("Both formFields and formFieldLabels must be provided.");
        }
        if(formFields.size() != formFieldLabels.size()) {
            throw new RuntimeException("formFields and formFieldLabels must be the same size.");
        }
        this.formFieldLabels = formFieldLabels;
        this.formFields = formFields;
    }

    /**
     * Set the top text.
     * @param topText String
     */
    public void setTopText(String topText)  {
        this.topText = topText;
    }

    /**
     * Set the bottom text.
     * @param bottomText String
     */
    public void setBottomText(String bottomText) {
        this.bottomText = bottomText;
    }
}
