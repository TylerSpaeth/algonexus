package com.github.tylerspaeth.engine.request.account;

import com.github.tylerspaeth.broker.response.AccountPnL;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class AccountPnLRequest extends AbstractEngineRequest<AccountPnL> {
    @Override
    protected AccountPnL execute() {
        return accountService.getAccountPnL();
    }
}
