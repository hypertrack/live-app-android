package com.hypertrack.lib.internal.transmitter.controls;

/**
 * Created by ulhas on 26/06/16.
 */
public abstract class UpdateControlsCallback {
    public abstract void onGoOnlineCommand(boolean resetSchedulingControls, boolean resetCollectionControls);

    public abstract void onGoActiveCommand(boolean resetSchedulingControls, boolean resetCollectionControls);

    public abstract void onFlushDataCommand();

    public abstract void onGoOfflineCommand();
}
