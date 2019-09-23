package com.hypertrack.live.map.htlocation;

import android.location.Location;

public interface HTLocationProvider {
	boolean startLocationProvider(HTLocationConsumer myLocationConsumer);

	void stopLocationProvider();

	Location getLastKnownLocation();

	void destroy();
}