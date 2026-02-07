package com.github.tylerspaeth.engine.request.datafeed;

import com.github.tylerspaeth.broker.ib.response.ContractDetails;
import com.github.tylerspaeth.common.data.entity.Symbol;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

import java.util.List;

public class GetContractDetailsForSymbolRequest extends AbstractEngineRequest<List<ContractDetails>> {

    private final Symbol symbol;

    public GetContractDetailsForSymbolRequest(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    protected List<ContractDetails> execute() {
        return dataFeedService.getContractDetailsForSymbol(symbol);
    }
}
