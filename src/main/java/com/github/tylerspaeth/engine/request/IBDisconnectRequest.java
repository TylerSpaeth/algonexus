package com.github.tylerspaeth.engine.request;

import com.github.tylerspaeth.broker.ib.IBSyncWrapper;

public class IBDisconnectRequest extends AbstractEngineRequest<Void> {
    @Override
    protected Void execute() {
        IBSyncWrapper.getInstance().disconnect();
        return null;
    }
}
