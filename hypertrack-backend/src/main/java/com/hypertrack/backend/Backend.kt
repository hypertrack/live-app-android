package com.hypertrack.backend

import com.hypertrack.backend.models.*

interface AbstractBackendProvider : HomeManagementApi {
    fun start(callback: ResultHandler<String>)
    fun stop()
    fun createTrip(tripConfig: TripConfig, callback: ResultHandler<ShareableTrip>)
    fun completeTrip(tripId: String, callback: ResultHandler<String>)
    fun getInviteLink(callback: ResultHandler<String>)
    fun getAccountName(callback: ResultHandler<String>)
}

interface HomeManagementApi {
    fun getHomeGeofenceLocation(resultHandler: ResultHandler<GeofenceLocation?>)
    fun updateHomeGeofence(homeLocation: GeofenceLocation, resultHandler: ResultHandler<Void?>)
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
