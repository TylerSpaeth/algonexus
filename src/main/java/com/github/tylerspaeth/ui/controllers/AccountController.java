package com.github.tylerspaeth.ui.controllers;

import com.github.tylerspaeth.broker.ib.IBAccountService;
import com.github.tylerspaeth.broker.response.AccountSummary;
import com.github.tylerspaeth.broker.response.Position;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AccountController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private final IBAccountService accountService = new IBAccountService();

    @FXML
    private ListView<Position> positionsListView;
    private final ObservableList<Position> positions = FXCollections.observableArrayList();

    @FXML
    private Text accountIDText;
    @FXML
    private Text availableFundsText;
    @FXML
    private Text totalCashValueText;
    @FXML
    private Text dailyPnLText;
    @FXML
    private Text unrealizedPnLText;
    @FXML
    private Text realizedPnLText;

    private static final String ACCOUNTID = "AccountID: {0}";
    private static final String AVAILABLE_FUNDS = "Available Funds: ${0}";
    private static final String TOTAL_CASH_VALUE = "Total Cash Value: ${0}";
    private static final String DAILY_PNL = "Daily P/L: ${0}";
    private static final String UNREALIZED_PNL = "Unrealized P/L: ${0}";
    private static final String REALIZED_PNL = "Realized P/L: ${0}";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Position> temp = accountService.getPositions();
        if(temp != null) {
            positions.addAll(temp);
        }
        positionsListView.setItems(positions);
        setTextFields();
    }

    private void setTextFields() {
        AccountSummary accountSummary = accountService.getAccountSummary();
        accountIDText.setText(MessageFormat.format(ACCOUNTID, accountSummary.accountID()));
        availableFundsText.setText(MessageFormat.format(AVAILABLE_FUNDS, accountSummary.availableFunds()));
        totalCashValueText.setText(MessageFormat.format(TOTAL_CASH_VALUE, accountSummary.totalCashValue()));

        var pnl = accountService.getAccountPnL();
        dailyPnLText.setText(MessageFormat.format(DAILY_PNL, pnl.dailyPnL()));
        unrealizedPnLText.setText(MessageFormat.format(UNREALIZED_PNL, pnl.unrealizedPnL()));
        realizedPnLText.setText(MessageFormat.format(REALIZED_PNL, pnl.realizedPnL()));
    }

}
