package com.hypertrack.live.map.util;

import android.location.LocationManager;

/**
 *
 * A class to check whether we want to use a location. If there are multiple location providers,
 * i.e. network and GPS, then you want to ignore network locations shortly after a GPS location
 * because you will get another GPS location soon.
 *
 */
public class NetworkLocationIgnorer {

	/** last time we got a location from the gps provider */
	private long mLastGps = 0;
	private long gpsWaitTime =20000;

	/**
	 * Whether we should ignore this location.
	 *
	 * @param pProvider
	 *            the provider that provided the location
	 * @param pTime
	 *            the time of the location
	 * @return true if we should ignore this location, false if not
	 */
	public boolean shouldIgnore(final String pProvider, final long pTime) {

		if (LocationManager.GPS_PROVIDER.equals(pProvider)) {
			mLastGps = pTime;
		} else {
			if (pTime < mLastGps + gpsWaitTime) {
				return true;
			}
		}

		return false;
	}
}
