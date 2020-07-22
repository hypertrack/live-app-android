package com.hypertrack.backend

import com.hypertrack.backend.models.*

interface AbstractBackendProvider {
    fun start(callback: ResultHandler<String>)
    fun stop()
    fun createTrip(tripConfig: TripConfig, callback: ResultHandler<ShareableTrip>)
    fun completeTrip(tripId: String, callback: ResultHandler<String>)
    fun createGeofence(location: GeofenceLocation, callback: ResultHandler<String>)
    fun getInviteLink(callback: ResultHandler<String>)
    fun getAccountName(callback: ResultHandler<String>)
    fun getHomeGeofence(callback: ResultHandler<GeofenceLocation>)
}

interface HomeManagementApi {
    fun getHomeGeofenceLocation(resultHandler: ResultHandler<GeofenceLocation?>)
    fun updateHomeGeofence()
}

interface AsyncTokenProvider {
    fun getAuthenticationToken(resultHandler: ResultHandler<String>)
}

interface GeofencesApiProvider {
    fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>)
    fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>)
    fun deleteGeofence(geofence_id: String)
}

interface ResultHandler<T> {
    fun onResult(result: T)
    fun onError(error: Exception)
}

