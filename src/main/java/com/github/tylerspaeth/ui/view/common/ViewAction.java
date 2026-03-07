package com.github.tylerspaeth.ui.view.common;

/**
 * Class the describes the actions that can occur after a view handles input.
 */
public class ViewAction {

    /**
     * Types of action that have occurred.
     */
    public enum Type {
        NONE,
        PUSH,
        POP,
        REPLACE
    }

    public final Type type;
    public final AbstractView view;

    private ViewAction(Type type, AbstractView view) {
        this.type = type;
        this.view = view;
    }

    /**
     * Effectively the "null" of ViewActions. This should be used any time no action should be taken.
     * @return ViewAction
     */
    public static ViewAction none() {
        return new ViewAction(Type.NONE, null);
    }

    /**
     * Push a new view to the view stack.
     * @param view View to be pushed to the stack.
     * @return ViewAction
     */
    public static ViewAction push(AbstractView view) {
        return new ViewAction(Type.PUSH, view);
    }

    /**
     * Pop the current view from the view stack.
     * @return ViewAction
     */
    public static ViewAction pop() {
        return new ViewAction(Type.POP, null);
    }

    /**
     * Replace the current view with the provided view.
     * @param view View to replace the current view with.
     * @return ViewAction
     */
    public static ViewAction replace(AbstractView view) {
        return new ViewAction(Type.REPLACE, view);
    }
}