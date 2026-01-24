package com.github.tylerspaeth.engine.request.account;

import com.github.tylerspaeth.broker.response.AccountSummary;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class AccountSummaryRequest extends AbstractEngineRequest<AccountSummary> {
    @Override
    protected AccountSummary execute() {
        return accountService.getAccountSummary();
    }
}
