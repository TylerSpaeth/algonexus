package com.github.tylerspaeth.ui.controllers;

import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.broker.response.Position;
import com.ib.controller.AccountSummaryTag;
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
        IBSyncWrapper ibSyncWrapper = IBSyncWrapper.getInstance();
        try {
            List<Position> temp = ibSyncWrapper.getPositions();
            if(temp != null) {
                positions.addAll(temp);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        positionsListView.setItems(positions);
        setTextFields();
    }

    private void setTextFields() {

        IBSyncWrapper ibSyncWrapper = IBSyncWrapper.getInstance();

        try {
            var accountInfo = ibSyncWrapper.getAccountSummary("ALL", List.of(AccountSummaryTag.AccountType, AccountSummaryTag.AvailableFunds, AccountSummaryTag.TotalCashValue));
            var accountID = accountInfo.accountSummaries.getFirst().accountID();
            var availableFunds = accountInfo.accountSummaries.get(1).value();
            var totalCashValue = accountInfo.accountSummaries.get(2).value();
            accountIDText.setText(MessageFormat.format(ACCOUNTID, accountID));
            availableFundsText.setText(MessageFormat.format(AVAILABLE_FUNDS, availableFunds));
            totalCashValueText.setText(MessageFormat.format(TOTAL_CASH_VALUE, totalCashValue));

            var pnl = ibSyncWrapper.getAccountPnL(accountID, "");
            dailyPnLText.setText(MessageFormat.format(DAILY_PNL, pnl.dailyPnL()));
            unrealizedPnLText.setText(MessageFormat.format(UNREALIZED_PNL, pnl.unrealizedPnL()));
            realizedPnLText.setText(MessageFormat.format(REALIZED_PNL, pnl.realizedPnL()));

        } catch (Exception e) {
            LOGGER.error("Failed to set account text fields.", e );
        }
    }

}
