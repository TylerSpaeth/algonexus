package com.github.tylerspaeth.engine.request.account;

import com.github.tylerspaeth.broker.response.Position;
import com.github.tylerspaeth.broker.response.PositionPnL;
import com.github.tylerspaeth.engine.request.AbstractEngineRequest;

public class PositionPnLRequest extends AbstractEngineRequest<PositionPnL> {

    private final Position position;

    public PositionPnLRequest(Position position) {
        this.position = position;
    }

    @Override
    protected PositionPnL execute() {
        return accountService.getPositionPnL(position);
    }
}
