package com.github.tylerspaeth;

import com.github.tylerspaeth.broker.datastream.DataFeedKey;
import com.github.tylerspaeth.broker.ib.IBService;
import com.github.tylerspaeth.common.enums.IntervalUnitEnum;
import com.github.tylerspaeth.common.enums.MarketDataType;
import com.github.tylerspaeth.ui.GUI;
import com.ib.client.Contract;
import com.ib.client.Types;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

//        String accountId;
//
//        IBService ibService = IBService.getInstance();
//        ibService.connect();
//
//        while(!ibService.isConnected()) {}
//
//        try {
//
//            ibService.setDataType(MarketDataType.FROZEN_DELAYED);
//
//            var symbols = ibService.getMatchingSymbols("USD");
//            System.out.println(symbols);
//
//            Contract contract = new Contract();
//            contract.symbol("BTC");
//            contract.currency("USD");
//            contract.secType(Types.SecType.CRYPTO);
//            contract.exchange("PAXOS");
//
//            var contractDetails = ibService.getContractDetails(contract);
//
//            var gbpusdkey = new DataFeedKey(null, "GBP", "CASH", "IDEALPRO", "USD");
//            var gbpusduuid = ibService.subscribeToDataFeed(gbpusdkey);
//
//            var eurusdkey = new DataFeedKey(null, "EUR", "CASH", "IDEALPRO", "USD");
//            var eurusduuid = ibService.subscribeToDataFeed(eurusdkey);
//
//            var btckey = new DataFeedKey(null, "BTC", "CRYPTO", "PAXOS", "USD");
//            var btcuuid = ibService.subscribeToDataFeed(btckey);
//
//            for(int i = 0; i < 100; i++) {
//                var val1 = ibService.readFromDataFeed(eurusdkey, eurusduuid, 1, IntervalUnitEnum.MINUTE);
//                var val2 = ibService.readFromDataFeed(gbpusdkey, gbpusduuid, 1, IntervalUnitEnum.MINUTE);
//                var val3 = ibService.readFromDataFeed(btckey, btcuuid, 1, IntervalUnitEnum.MINUTE);
//                if(val1.size() > 0) {
//                    System.out.println("val1 " + val1.getFirst());
//                }
//                if(val2.size() > 0) {
//                    System.out.println("val2 " + val2.getFirst());
//                }
//                if(val3.size() > 0) {
//                    System.out.println("val3 " + val3.getFirst());
//                }
//                Thread.sleep(5000);
//            }
//
//            ibService.unsubscribeFromDataFeed(gbpusdkey, gbpusduuid);
//            ibService.unsubscribeFromDataFeed(eurusdkey, eurusduuid);
//            ibService.unsubscribeFromDataFeed(btckey, btcuuid);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        Application.launch(GUI.class, "");

        //ibService.disconnect();

    }
}