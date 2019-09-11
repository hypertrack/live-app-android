package com.hypertrack.live.map;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hypertrack.live.R;
import com.hypertrack.live.debug.DebugHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripsManager {
    private final SharedPreferences sharedPreferences;
    private Marker destMarker;
    private LatLng destLatLng;
    private Marker tripDestMarker;
    private Polyline tripRoutPolyline;
    private int polylineColor;

    public LatLng getDestLatLng() {
        return destLatLng;
    }

    public TripsManager(Context context) {
        sharedPreferences = DebugHelper.getSharedPreferences(context);
        polylineColor = context.getResources().getColor(R.color.colorPrimary);
    }

    public void addTo(final GoogleMap googleMap) {
        removeFrom(googleMap);

        if (destLatLng != null) {
            destMarker = googleMap.addMarker(new MarkerOptions()
                    .position(destLatLng)
                    .draggable(true));
        }
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                destLatLng = marker.getPosition();
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                destLatLng = latLng;
                if (destMarker == null) {
                    destMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .draggable(true));
                }
                destMarker.setPosition(latLng);
            }
        });


        try {
            JSONObject currentTrip = new JSONObject(sharedPreferences.getString("current_trip", ""));
            JSONArray destination = currentTrip.getJSONObject("destination").getJSONObject("geometry").getJSONArray("coordinates");
            tripDestMarker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(destination.getDouble(1), destination.getDouble(0)))
                    .icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_myplaces)));

            List<LatLng> latLngs = new ArrayList<>();
            JSONArray coordinates = currentTrip.getJSONObject("estimate").getJSONObject("route").getJSONObject("polyline").getJSONArray("coordinates");
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray lonLat = coordinates.getJSONArray(i);
                latLngs.add(new LatLng(lonLat.getDouble(1), lonLat.getDouble(0)));
            }
            tripRoutPolyline = googleMap.addPolyline(new PolylineOptions().width(7).color(polylineColor).addAll(latLngs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeFrom(GoogleMap googleMap) {
        if (destMarker != null) {
            destMarker.remove();
            destMarker = null;
        }
        if (tripDestMarker != null) {
            tripDestMarker.remove();
            tripDestMarker = null;
        }
        if (tripRoutPolyline != null) {
            tripRoutPolyline.remove();
            tripRoutPolyline = null;
        }
    }


}
