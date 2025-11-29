package com.github.tylerspaeth.broker;

import com.github.tylerspaeth.broker.request.AccountSummaryRequest;
import com.github.tylerspaeth.broker.request.PositionPnLRequest;
import com.github.tylerspaeth.broker.response.AccountPnLResponse;
import com.github.tylerspaeth.broker.response.AccountSummaryResponse;
import com.github.tylerspaeth.broker.response.PositionPnLResponse;
import com.github.tylerspaeth.broker.response.PositionResponse;

public interface IAccountService {

    /**
     * Get account information.
     * @param accountSummaryRequest A request for the information to receive
     * @return AccountSummaryResponse object with account info
     * @throws Exception if something fails while making the request
     */
    AccountSummaryResponse getAccountSummary(AccountSummaryRequest accountSummaryRequest) throws Exception;

    /**
     * Gets all the positions for the active account
     * @return PositionResponse with list of Positions
     * @throws Exception if something fails while making the request
     */
    PositionResponse getPositions() throws Exception;

    /**
     * Get PnL information across the entire account
     * @param accountId ID of the account
     * @return PnL information for the account
     * @throws Exception if something fails while making the request
     */
    AccountPnLResponse getAccountPnL(String accountId) throws Exception;

    /**
     * Get PnL information for a specific position.
     * @param positionPnLRequest Request object defining the position to search for
     * @return PnL information for the position
     * @throws Exception if something fails while making the request
     */
    PositionPnLResponse getPositionPnL(PositionPnLRequest positionPnLRequest) throws Exception;

}
