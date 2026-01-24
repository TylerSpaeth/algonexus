package com.github.tylerspaeth.engine.request.account;

import com.github.tylerspaeth.broker.response.Position;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

import java.util.List;

public class PositionsRequest extends AbstractEngineRequest<List<Position>> {
    @Override
    protected List<Position> execute() {
        return accountService.getPositions();
    }
}
