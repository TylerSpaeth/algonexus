package com.github.tylerspaeth.engine.request;

import com.github.tylerspaeth.broker.ib.IBSyncWrapper;

public class IBConnectionRequest extends AbstractEngineRequest<Void> {
    @Override
    protected Void execute() {
        IBSyncWrapper.getInstance().connect();
        return null;
    }
}
