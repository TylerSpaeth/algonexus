package com.github.tylerspaeth.broker;

import com.github.tylerspaeth.broker.response.AccountPnL;
import com.github.tylerspaeth.broker.response.AccountSummary;
import com.github.tylerspaeth.broker.response.Position;
import com.github.tylerspaeth.broker.response.PositionPnL;

import java.util.List;

/**
 * Account related functionality
 */
public interface IAccountService {

    /**
     * Gets the account summary details for the active account.
     * @return AccountSummary
     */
    AccountSummary getAccountSummary();

    /**
     * Gets all the open positions the active account has.
     * @return List of Positions
     */
    List<Position> getPositions();

    /**
     * Gets the account profit and loss numbers for the provided account.
     * @return AccountPnL
     */
    AccountPnL getAccountPnL();

    /**
     * Gets the profit and loss numbers for the provided position.
     * @param position The position to search for.
     * @return PositionPnL
     */
    PositionPnL getPositionPnL(Position position);

}
