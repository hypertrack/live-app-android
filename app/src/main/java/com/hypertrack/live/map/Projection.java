package com.hypertrack.live.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.live.map.util.TileSystem;

public class Projection {

	private final float mZoomLevelProjection;
	private final LatLng center;

	public Projection(GoogleMap googleMap) {
		mZoomLevelProjection = googleMap.getCameraPosition().zoom;
		center = googleMap.getCameraPosition().target;
	}

	public float metersToEquatorPixels(final float meters) {
		return metersToPixels(meters, 0, mZoomLevelProjection);
	}

	public float metersToPixels(final float meters) {
		return metersToPixels(meters, center.latitude, mZoomLevelProjection);
	}

	public float metersToPixels(final float meters, final double latitude, final double zoomLevel) {
		return (float) (meters / TileSystem.GroundResolution(latitude, zoomLevel));
	}

}