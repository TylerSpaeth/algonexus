package com.github.tylerspaeth.ib;

public class IBSyncAPI {

    private final IBConnection ibConnection = new IBConnection();

    public void connect() {
        ibConnection.connect();
    }

    public void disconnect() {
        ibConnection.disconnect();
    }

}
