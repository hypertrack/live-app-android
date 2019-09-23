package com.hypertrack.live.map.htlocation;

import android.location.Location;

public interface HTLocationConsumer {
	/**
	 * Call when a provider has a new location to consume. This can be called on any thread.
	 */
	void onLocationChanged(Location location, HTLocationProvider source);
}