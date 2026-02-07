package com.github.tylerspaeth.engine.request;

import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.broker.ib.response.ContractDetails;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.common.enums.AssetTypeEnum;
import com.github.tylerspaeth.engine.EngineCoordinator;
import com.github.tylerspaeth.engine.request.datafeed.GetContractDetailsForSymbolRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class IBConnectionRequest extends AbstractEngineRequest<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBConnectionRequest.class);

    private final SymbolDAO symbolDAO = new SymbolDAO();
    private final EngineCoordinator engineCoordinator;

    public IBConnectionRequest(EngineCoordinator engineCoordinator) {
        this.engineCoordinator = engineCoordinator;
    }

    @Override
    protected Void execute() {
        IBSyncWrapper.getInstance().connect();
        while(!IBSyncWrapper.getInstance().isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        setIBConIDs();

        return null;
    }

    /**
     * Set the IBConIDs on all the symbols in the DB.
     */
    private void setIBConIDs() {

        List<Symbol> symbols = symbolDAO.getAllSymbols();

        symbols.forEach(symbol -> {
            try {
                List<ContractDetails> contractDetails = engineCoordinator.submitRequest(new GetContractDetailsForSymbolRequest(symbol));

                if(contractDetails.isEmpty()) {
                    LOGGER.warn("No contract details found for symbol: {}.", symbol);
                } else if(contractDetails.size() == 1) {
                    Integer ibConID = contractDetails.getFirst().getContract().getConid();

                    if(!Objects.equals(symbol.getIbConID(), ibConID)) {
                        LOGGER.info("Updating symbol {} IBConID from {} to {}", symbol, symbol.getIbConID(), ibConID);
                        symbol.setIbConID(ibConID);
                        symbolDAO.update(symbol);
                    }
                } else {
                    if(symbol.getAssetType() == AssetTypeEnum.FUTURES) {
                        contractDetails.sort(Comparator.comparing(contractDetail -> contractDetail.getContract().getLastTradeDate()));

                        Integer ibConID = contractDetails.getFirst().getContract().getConid();

                        if(!Objects.equals(symbol.getIbConID(), ibConID)) {
                            LOGGER.info("Updating symbol {} IBConID from {} to {}", symbol, symbol.getIbConID(), ibConID);
                            symbol.setIbConID(ibConID);
                            symbolDAO.update(symbol);
                        }
                    } else {
                        List<Integer> availableConIDs = contractDetails.stream().map(contractDetail -> contractDetail.getContract().getConid()).toList();
                        LOGGER.warn("Multiple contract details found for non futures symbol. Review the available options and set manually. Symbol: {}. Available IBConIDs: {}", symbol, availableConIDs);
                    }
                }

            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Setting IBConID for symbol: {} failed.", symbol);
            }
        });

    }
}
