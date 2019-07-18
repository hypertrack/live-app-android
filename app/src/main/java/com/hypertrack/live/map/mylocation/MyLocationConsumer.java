package com.hypertrack.live.map.mylocation;

import android.location.Location;

public interface MyLocationConsumer {
	/**
	 * Call when a provider has a new location to consume. This can be called on any thread.
	 */
	void onLocationChanged(Location location, MyLocationProvider source);
}