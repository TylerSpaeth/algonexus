package com.github.tylerspaeth.engine.request;

import com.github.tylerspaeth.broker.ib.IBSyncWrapper;

public class IBConnectionRequest extends AbstractEngineRequest<Void> {
    @Override
    protected Void execute() {
        IBSyncWrapper.getInstance().connect();
        while(!IBSyncWrapper.getInstance().isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
