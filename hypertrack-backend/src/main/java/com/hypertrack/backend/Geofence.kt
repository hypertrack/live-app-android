package com.hypertrack.backend

import com.google.gson.annotations.SerializedName

data class GeofenceLocation(
        val latitude: Double,
        val longitude: Double
)

data class GeofenceResponse(
        @SerializedName("geofence_id") val geofenceId: String
)