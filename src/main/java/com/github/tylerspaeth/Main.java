package com.github.tylerspaeth;

import com.github.tylerspaeth.broker.datastream.DataFeedKey;
import com.github.tylerspaeth.broker.ib.IBService;
import com.github.tylerspaeth.common.enums.MarketDataType;
import com.github.tylerspaeth.ui.GUI;
import com.ib.client.Contract;
import com.ib.client.Types;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        String accountId;

        IBService ibService = new IBService();
        ibService.connect();

        while(!ibService.isConnected()) {}

        try {

            ibService.setDataType(MarketDataType.FROZEN_DELAYED);

            var symbols = ibService.getMatchingSymbols("USD");
            System.out.println(symbols);

            Contract contract = new Contract();
            contract.symbol("GBP");
            contract.currency("USD");
            contract.secType(Types.SecType.CASH);
            contract.exchange("IDEALPRO");

            var contractDetails = ibService.getContractDetails(contract);

            var gbpusdkey = new DataFeedKey(null, "GBP", "CASH", "IDEALPRO", "USD");
            var gbpusduuid = ibService.subscribeToDataFeed(gbpusdkey);

            var eurusdkey = new DataFeedKey(null, "EUR", "CASH", "IDEALPRO", "USD");
            var eurusduuid = ibService.subscribeToDataFeed(eurusdkey);

            for(int i = 0; i < 5; i++) {
                var val1 = ibService.readFromDataFeed(eurusdkey, eurusduuid);
                var val2 = ibService.readFromDataFeed(gbpusdkey, gbpusduuid);
                if(val1.size() > 0) {
                    System.out.println("val1 " + val1.getFirst());
                }
                if(val2.size() > 0) {
                    System.out.println("val2 " + val2.getFirst());
                }
                Thread.sleep(5000);
            }

            ibService.unsubscribeFromDataFeed(gbpusdkey, gbpusduuid);
            ibService.unsubscribeFromDataFeed(eurusdkey, eurusduuid);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Application.launch(GUI.class, "");

        ibService.disconnect();

    }
}