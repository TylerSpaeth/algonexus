package com.github.tylerspaeth.ui;

import com.github.tylerspaeth.common.data.entity.User;
import com.github.tylerspaeth.engine.EngineCoordinator;

/**
 * Class representing all application context that is available to the UI layer.
 */
public class UIContext {

    public final EngineCoordinator engineCoordinator;

    public User activeUser = null;

    public UIContext(EngineCoordinator engineCoordinator) {
        this.engineCoordinator = engineCoordinator;
    }

}
