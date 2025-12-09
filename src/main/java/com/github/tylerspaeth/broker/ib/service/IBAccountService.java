package com.github.tylerspaeth.broker.ib.service;

import com.github.tylerspaeth.broker.IAccountService;
import com.github.tylerspaeth.broker.ib.IBMapper;
import com.github.tylerspaeth.broker.ib.IBSyncWrapper;
import com.github.tylerspaeth.broker.response.AccountPnL;
import com.github.tylerspaeth.broker.response.AccountSummary;
import com.github.tylerspaeth.broker.response.Position;
import com.github.tylerspaeth.broker.response.PositionPnL;
import com.github.tylerspaeth.common.data.dao.SymbolDAO;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.controller.AccountSummaryTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IBAccountService implements IAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBAccountService.class);

    private String accountID;

    private final IBSyncWrapper wrapper;
    private final SymbolDAO symbolDAO;

    public IBAccountService() {
        wrapper = IBSyncWrapper.getInstance();
        symbolDAO = new SymbolDAO();
    }

    @Override
    public AccountSummary getAccountSummary() {
        try {
            List<AccountSummaryTag> tags = getSummaryTags();
            List<com.github.tylerspaeth.broker.ib.response.AccountSummary> summary = wrapper.getAccountSummary("All", tags);
            return extractAccountSummary(summary);
        } catch(Exception e) {
            LOGGER.error("Failed to getAccountSummary", e);
            return null;
        }
    }

    @Override
    public List<Position> getPositions() {
        try {
            List<Position> newPositions = new ArrayList<>();
            List<com.github.tylerspaeth.broker.ib.response.Position> oldPositions = wrapper.getPositions();
            if(oldPositions == null) {
                return List.of();
            }
            for(com.github.tylerspaeth.broker.ib.response.Position position : oldPositions) {
                Contract contract = position.contract();

                Symbol mappedSymbol = symbolDAO.getSymbolByCriteria(contract.symbol(), contract.exchange(), IBMapper.mapSecTypeToAssetType(contract.secType()));
                if(mappedSymbol == null) {
                    LOGGER.warn("Failed to find Symbol (Ticker:{}, Exchange:{}, SecType:{}", contract.symbol(), contract.exchange(), contract.secType());
                }

                newPositions.add(new Position(mappedSymbol, position.position().value().doubleValue(), position.avgCost()));
            }
            return newPositions;
        } catch(Exception e) {
            LOGGER.error("Failed to getPositions", e);
            return List.of();
        }
    }

    @Override
    public AccountPnL getAccountPnL() {
        try {
            if(accountID == null) {
                var newAccountID = getAccountID();
                if(newAccountID == null) {
                    throw new Exception("Failed to load accountID");
                }
                accountID = newAccountID;
            }
            return IBMapper.mapAccountPnL(wrapper.getAccountPnL(accountID, ""));
        } catch(Exception e) {
            LOGGER.error("Failed to getAccountPnL", e);
            return null;
        }
    }

    @Override
    public PositionPnL getPositionPnL(Position position) {
        try {
            if(accountID == null) {
                var newAccountID = getAccountID();
                if(newAccountID == null) {
                    throw new Exception("Failed to load accountID");
                }
                accountID = newAccountID;
            }
            return IBMapper.mapPositionPnL(wrapper.getPositionPnL(accountID, "", getConIdFromSymbol(position.symbol())));
        } catch(Exception e) {
            LOGGER.error("Failed to getPositionPnL", e);
            return null;
        }
    }

    /**
     * Gets a list of AccountSummaryTags that are used to build an AccountSummary object
     * @return List of AccountSummaryTag
     */
    private List<AccountSummaryTag> getSummaryTags() {
        return List.of(AccountSummaryTag.AvailableFunds,
                AccountSummaryTag.ExcessLiquidity,
                AccountSummaryTag.BuyingPower,
                AccountSummaryTag.MaintMarginReq,
                AccountSummaryTag.SettledCash,
                AccountSummaryTag.GrossPositionValue,
                AccountSummaryTag.TotalCashValue);
    }

    /**
     * Extract an AccountSummary object from the list of IB AccountSummary objects
     * @param accountSummaryList List of IB AccountSummary objects
     * @return A single unified AccountSummary object
     */
    private AccountSummary extractAccountSummary(List<com.github.tylerspaeth.broker.ib.response.AccountSummary> accountSummaryList) {

        var map = accountSummaryList.stream().collect(Collectors.toMap(com.github.tylerspaeth.broker.ib.response.AccountSummary::tag, Function.identity()));

        if(!accountSummaryList.isEmpty()) {
            accountID = accountSummaryList.getFirst().accountID();
        }

        var availableFundsTemp = map.get(AccountSummaryTag.AvailableFunds.name());
        Double availableFunds = null;
        if(availableFundsTemp != null) {
            availableFunds = Double.valueOf(availableFundsTemp.value());
        }

        var excessLiquidityTemp = map.get(AccountSummaryTag.ExcessLiquidity.name());
        Double excessLiquidity = null;
        if(excessLiquidityTemp != null) {
            excessLiquidity = Double.valueOf(excessLiquidityTemp.value());
        }

        var buyingPowerTemp = map.get(AccountSummaryTag.BuyingPower.name());
        Double buyingPower = null;
        if(buyingPowerTemp != null) {
            buyingPower = Double.valueOf(buyingPowerTemp.value());
        }

        var maintMarginReqTemp = map.get(AccountSummaryTag.MaintMarginReq.name());
        Double maintMarginReq = null;
        if(maintMarginReqTemp != null) {
            maintMarginReq = Double.valueOf(maintMarginReqTemp.value());
        }

        var settledCashTemp = map.get(AccountSummaryTag.SettledCash.name());
        Double settledCash = null;
        if(settledCashTemp != null) {
            settledCash = Double.valueOf(settledCashTemp.value());
        }

        var grossPositionValueTemp = map.get(AccountSummaryTag.GrossPositionValue.name());
        Double grossPositionValue = null;
        if(grossPositionValueTemp != null) {
            grossPositionValue = Double.valueOf(grossPositionValueTemp.value());
        }

        var totalCashValueTemp = map.get(AccountSummaryTag.TotalCashValue.name());
        Double totalCashValue = null;
        if(totalCashValueTemp != null) {
            totalCashValue = Double.valueOf(totalCashValueTemp.value());
        }

        return new AccountSummary(accountID, availableFunds, excessLiquidity, buyingPower, maintMarginReq, settledCash, grossPositionValue, totalCashValue);
    }

    /**
     * Attempts to get the accountID of the active account.
     * @return AccountID string
     */
    private String getAccountID() {
        // TODO implement an intentional way of doing this rather than doing a workaround
        try {
            return wrapper.getAccountSummary("", List.of(AccountSummaryTag.AccountType)).getFirst().accountID();
        } catch(Exception e) {
            LOGGER.error("Failed to loadAccountID", e);
            return null;
        }
    }

    /**
     * Gets the conId on the Contract corresponding to the symbol.
     * @param symbol Symbol to search off of
     * @return conId if one can be found
     * @throws Exception If a single contract is not found for the provided symbol
     */
    private Integer getConIdFromSymbol(Symbol symbol) throws Exception {
        Contract contract = new Contract();
        contract.symbol(symbol.getTicker());
        contract.exchange(symbol.getExchange().getName());
        contract.secType(IBMapper.mapAssetTypeToSecType(symbol.getAssetType()));
        contract.currency("USD");

        List<ContractDetails> details = wrapper.getContractDetails(contract);

        if(details.size() != 1) {
            LOGGER.error("Failed to get an individual contract from symbol. Expected 1, got {}.", details.size());
            throw new RuntimeException("Failed to get an individual contract.");
        }

        return details.getFirst().conid();
    }

}
