package com.hypertrack.backend

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.net.HttpURLConnection

class PublicKeyAuthorizedBackendProvider(
        context: Context, publishableKey: String,
        private val deviceID: String,
        baseUrl: String = "https://live-app-backend.htprod.hypertrack.com/",
        authUrl: String = "https://live-api.htprod.hypertrack.com/authenticate"
) : AbstractBackendProvider {

    private val queue = Volley.newRequestQueue(context)
    private val gson = Gson()
    private val internalApiIssuesTokenProvider = InternalApiTokenProvider(queue, deviceID, publishableKey, gson, authUrl)
    val backendProvider = VolleyBasedProvider(gson, queue, internalApiIssuesTokenProvider, baseUrl)

    override fun start(callback: ResultHandler<String>) {
        Log.i(TAG, "start $deviceID")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.start(deviceID, callback) }
        )
        backendProvider.start(deviceID, retryCallback)
    }

    override fun stop() {
        Log.i(TAG, "stop $deviceID")
        val retryCallback = wrapCallback<String>(
                null,
                Runnable { backendProvider.stop(deviceID, null) }
        )
        backendProvider.stop(deviceID, retryCallback)

    }

    override fun createTrip(tripConfig: TripConfig, callback: ResultHandler<ShareableTrip>) {
        Log.i(TAG, "Creating trip with config $tripConfig")
        val retryCallback = wrapCallback<ShareableTrip>(
                callback,
                Runnable  { backendProvider.createTrip(tripConfig, callback) }
        )
        backendProvider.createTrip(tripConfig, retryCallback)
    }

    override fun completeTrip(tripId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "Complete trip $tripId")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.completeTrip(tripId, callback) }
        )
        backendProvider.completeTrip(tripId, retryCallback)

    }

    override fun getInviteLink(callback: ResultHandler<String>) {
        Log.i(TAG, "getInviteLink")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.getInviteLink(callback) }
        )
        backendProvider.getInviteLink(retryCallback)
    }

    override fun getAccountName(callback: ResultHandler<String>) {
        Log.d(TAG, "Requesting account email")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.getAccountEmail(callback) }
        )
        backendProvider.getAccountEmail(retryCallback)
    }

    override fun createGeofence(location: GeofenceLocation, callback: ResultHandler<String>) {
        Log.i(TAG, "Get geofences")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.createGeofence(location, deviceID, callback) }
        )
        backendProvider.createGeofence(location, deviceID, retryCallback)
    }

    override fun getHomeGeofence(callback: ResultHandler<GeofenceLocation>) {
        TODO("Not yet implemented")
    }

    private fun <T> wrapCallback(callback: ResultHandler<T>?, actualCall: Runnable): ResultHandler<T> {
        return object : ResultHandler<T> {
            override fun onResult(result: T): Unit  {callback?.onResult(result)}

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<T>(error, callback) { actualCall.run() }
        }
    }

    private fun <T> getErrorHandlerWithTokenAutoRefresh(
            error: Exception,
            callback: ResultHandler<T>?,
            retryCall: () -> Unit
    ) {
        when (error) {
            is VolleyError -> {
                if (error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    internalApiIssuesTokenProvider.refreshToken(queue) { retryCall() }
                    return
                }
            }
        }
        callback?.onError(error)
    }

    companion object { const val TAG = "PublicKeyAuthorizedBP" }
}

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

interface AsyncTokenProvider {
    fun getAuthenticationToken(resultHandler: ResultHandler<String>)
}

interface ResultHandler<T> {
    fun onResult(result: T)
    fun onError(error: Exception)
}

class ShareableTrip(val shareUrl: String, val embedUrl: String, val tripId: String, val remainingDuration: Int?)