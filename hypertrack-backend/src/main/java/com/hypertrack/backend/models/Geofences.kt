package com.hypertrack.backend.models

import com.google.gson.annotations.SerializedName

data class GeofenceLocation(
        val latitude: Double,
        val longitude: Double
)

data class GeofenceResponse(
        @SerializedName("geofence_id") val geofenceId: String
)

data class GeofenceParams(
        @SerializedName("geofences") val geofences: Set<GeofenceProperties>,
        @SerializedName("device_id") val deviceId: String
)

data class GeofenceProperties(
        @SerializedName("geometry") val geometry: Geometry,
        @SerializedName("metadata") val metadata: Map<String, Any>,
        @SerializedName("radius") val radius: Int?
)

data class Geofence(
        @SerializedName("all_devices") val all_devices : Boolean?,
        @SerializedName("created_at") val created_at : String,
        @SerializedName("delete_at") val delete_at : String?,
        @SerializedName("device_id") val device_id : String,
        @SerializedName("device_ids") val device_ids : List<String>?,
        @SerializedName("geofence_id") val geofence_id : String,
        @SerializedName("geometry") val geometry : Geometry,
        @SerializedName("metadata") val metadata : Map<String, Any>?,
        @SerializedName("radius") val radius : Int,
        @SerializedName("archived") val archived : Boolean?,
        @SerializedName("single_use") val single_use : Boolean
) {
    val latitude: Double
        get() = geometry.latitude
    val longitude: Double
        get() = geometry.longitude

    val type: String
        get() = geometry.type
}

class Point (
        @SerializedName("coordinates") override val coordinates : List<Double>
) : Geometry() {
    override val type: String
        get() = "Point"

    override val latitude: Double
        get() = coordinates[1]

    override val longitude: Double
        get() = coordinates[0]
}

class Polygon (
        @SerializedName("coordinates") override val coordinates : List<List<Double>>
) : Geometry() {
    override val type: String
        get() = "Polygon"
    override val latitude: Double
        get() = coordinates.map { it[1] }.average()
    override val longitude: Double
        get() = coordinates.map { it[0] }.average()
}

abstract class Geometry {
    abstract val coordinates: List<*>
    abstract val type: String
    abstract val latitude: Double
    abstract val longitude: Double
}