package com.github.tylerspaeth.ib;

/**
 * Functions to be called by the corresponding IB callback.
 */
public interface IIBConnectionListener {

    void onConnect();
    void onDisconnect();
    void onNextValidId(int i);

}
