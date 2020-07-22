package com.hypertrack.backend

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.hypertrack.backend.deprecated.InternalApiTokenProvider
import com.hypertrack.backend.deprecated.VolleyBasedProvider
import com.hypertrack.backend.models.GeofenceLocation
import com.hypertrack.backend.models.ShareableTrip
import com.hypertrack.backend.models.TripConfig
import java.net.HttpURLConnection


class HybridBackendProvider(
        context: Context, publishableKey: String,
        private val deviceID: String,
        baseUrl: String,
        authUrl: String
) : AbstractBackendProvider {

    private val queue = Volley.newRequestQueue(context)
    private val gson = Injector.gson
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
        Log.i(TAG, "Create geofence Location")
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
            override fun onResult(result: T) {callback?.onResult(result)}

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

    companion object {
        const val TAG = "HybridBackendProvider"

        @JvmStatic
        fun getInstance(context: Context, deviceID: String, publishableKey: String) : HybridBackendProvider {
            return HybridBackendProvider(context, publishableKey, deviceID, Injector.baseUrl, Injector.authUrl)
        }
    }
}