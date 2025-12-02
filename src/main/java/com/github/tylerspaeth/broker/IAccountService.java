package com.github.tylerspaeth.broker;

import com.github.tylerspaeth.broker.response.*;
import com.ib.controller.AccountSummaryTag;

import java.util.List;

public interface IAccountService {

    /**
     * Get account information.
     * @param accountSummaryTags The tags for the information to receive
     * @return AccountSummaryResponse object with account info
     * @throws Exception if something fails while making the request
     */
    AccountSummaryResponse getAccountSummary(List<AccountSummaryTag> accountSummaryTags) throws Exception;

    /**
     * Gets all the positions for the active account
     * @return list of Positions
     * @throws Exception if something fails while making the request
     */
    List<Position> getPositions() throws Exception;

    /**
     * Get PnL information across the entire account
     * @param accountId ID of the account
     * @return PnL information for the account
     * @throws Exception if something fails while making the request
     */
    AccountPnLResponse getAccountPnL(String accountId) throws Exception;

    /**
     * Get PnL information for a specific position.
     * @param accountId the accountID
     * @param conId the contractID
     * @return PnL information for the position
     * @throws Exception if something fails while making the request
     */
    PositionPnLResponse getPositionPnL(String accountId, int conId) throws Exception;

}
