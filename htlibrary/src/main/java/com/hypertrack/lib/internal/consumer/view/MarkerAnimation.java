package com.hypertrack.lib.internal.consumer.view;

/**
 * Created by ulhas on 27/06/16.
 */

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/** package */ class MarkerAnimation {

    private static LatLngInterpolator latLngInterpolator = new LatLngInterpolator();

    static void animateMarker(final Handler handler, final Marker marker, final LatLng finalPosition, final float durationInMs) {

        final LatLng startPosition = marker.getPosition();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new DecelerateInterpolator();

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}
