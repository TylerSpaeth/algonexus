package com.github.tylerspaeth;

import com.github.tylerspaeth.ib.IBWrapper;
import com.github.tylerspaeth.ui.GUI;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        IBWrapper wrapper = new IBWrapper();
        wrapper.connect();

        Application.launch(GUI.class, "");

        wrapper.disconnect();

    }
}