package com.github.tylerspaeth.ui.controllers;

import com.github.tylerspaeth.broker.ib.IBService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HomeController {

    @FXML
    private Button connectButton;

    @FXML
    private void connect() {
        IBService.getInstance().connect();
    }

}
