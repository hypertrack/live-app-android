package com.hypertrack.backend.deprecated

import android.content.Context
import android.util.Log
import androidx.annotation.GuardedBy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.hypertrack.backend.AsyncTokenProvider
import com.hypertrack.backend.ResultHandler
import com.hypertrack.backend.models.GeofenceLocation
import com.hypertrack.backend.models.ShareableTrip
import com.hypertrack.backend.models.TripConfig


class VolleyBasedProvider(
        private val gson: Gson,
        private val queue: RequestQueue,
        private val tokenProvider: AsyncTokenProvider,
        private val baseUrl: String
) {
    private val defaultRetryPolicy: DefaultRetryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

    fun start(deviceId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "start $deviceId")
        if (deviceId.isEmpty()) {
            callback.onError(java.lang.Exception("Can't start with empty deviceId"))
            return
        }

        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedStartTrackingRequest(deviceId, result, callback)

            override fun onError(error: Exception) = callback.onError(error)

        })
    }

    fun stop(deviceId: String, callback: ResultHandler<String>?) {
        Log.i(TAG, "stop $deviceId")
        if (deviceId.isEmpty()) {
            callback?.onError(java.lang.Exception("Can't stop with empty deviceId"))
            return
        }

        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedStopTrackingRequest(deviceId, result, callback)

            override fun onError(error: Exception) {callback?.onError(error)}

        })
    }

    fun createTrip(tripConfig: TripConfig, callback: ResultHandler<ShareableTrip>) {
        Log.i(TAG, "Create trip with config $tripConfig")
        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {

            override fun onResult(result: String) =
                    scheduleAuthenticatedCreateTripRequest(tripConfig, result, callback)

            override fun onError(error: Exception) = callback.onError(error)
        })

    }

    fun completeTrip(tripId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "Complete trip $tripId")
        if (tripId.isEmpty()) {
            callback.onError(java.lang.Exception("Can't complete trip with empty id"))
            return
        }

        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedCompletionTripRequest(tripId, result, callback)

            override fun onError(error: Exception) = callback.onError(error)

        })
    }

    fun getInviteLink(callback: ResultHandler<String>) {
        Log.i(TAG, "getInviteLink")
        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedGetInviteLinkRequest(result, callback)

            override fun onError(error: Exception) = callback.onError(error)

        })
    }

    fun getAccountEmail(callback: ResultHandler<String>) {
        Log.i(TAG, "getAccountEmail")
        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedGetAccountEmailRequest(result, callback)

            override fun onError(error: Exception) = callback.onError(error)

        })
    }

    fun createGeofence(location: GeofenceLocation, deviceId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "createGeofence $deviceId for location $location")
        if (deviceId.isEmpty()) {
            callback.onError(java.lang.Exception("Can't create geofences with empty deviceId"))
            return
        }

        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedCreateGeofenceRequest(location, deviceId, result, callback)

            override fun onError(error: Exception) = callback.onError(error)

        })
    }

    private fun scheduleAuthenticatedStartTrackingRequest(deviceId: String, tokenString: String, callback: ResultHandler<String>) {
        val request = StartTrackingRequest(
                deviceId, tokenString,
                Response.Listener { callback.onResult(deviceId) },
                Response.ErrorListener { error -> callback.onError(error) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding start request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedStopTrackingRequest(deviceId: String, tokenString: String, callback: ResultHandler<String>?) {
        val request = StopTrackingRequest(
                deviceId, tokenString,
                Response.Listener { callback?.onResult(deviceId) },
                Response.ErrorListener { error -> callback?.onError(error as Exception) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding stop request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedCreateTripRequest(tripConfig: TripConfig, tokenString: String, callback: ResultHandler<ShareableTrip>) {
        val request = CreateTripRequest(
                tripConfig, gson, tokenString,
                Response.Listener { trip -> callback.onResult(trip) },
                Response.ErrorListener { error -> callback.onError(error as Exception) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding create trip request to queue")
        queue.add(request)
    }

    private fun scheduleAuthenticatedCompletionTripRequest(tripId: String, tokenString: String, callback: ResultHandler<String>) {
        Log.d(TAG, "Scheduling Authentication request for trip $tripId with token $tokenString")
        val request = CompleteTripRequest(
                tripId, tokenString,
                Response.Listener { callback.onResult(tripId) },
                Response.ErrorListener { error -> callback.onError(error as Exception) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding complete trip request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedGetInviteLinkRequest(tokenString: String, callback: ResultHandler<String>) {
        Log.d(TAG, "Requesting deeplink with token $tokenString")
        val request = GetDeeplinkRequest(
                tokenString, Response.Listener { deeplink ->
            Log.d(TAG, "Got deeplink $deeplink")
            callback.onResult(deeplink)
        },
                Response.ErrorListener { error -> callback.onError(error as Exception) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding deeplink request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedGetAccountEmailRequest(tokenString: String, callback: ResultHandler<String>) {
        Log.d(TAG, "Requesting account email with token $tokenString")
        val request = GetAccountEmailRequest(
                tokenString, Response.Listener { email ->
            Log.d(TAG, "Got email $email")
            callback.onResult(email)
        },
                Response.ErrorListener { error -> callback.onError(error as Exception) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding account email request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedCreateGeofenceRequest(
            location: GeofenceLocation, deviceId: String, tokenString: String, callback: ResultHandler<String>?
    ) {
        val request = CreateGeofencesRequest(location,
                deviceId, gson, tokenString,
                Response.Listener { callback?.onResult(deviceId) },
                Response.ErrorListener { error -> callback?.onError(error as Exception) },
                baseUrl
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding get geofences request to queue")
        queue.add(request)

    }

    companion object {
        const val TAG = "BackendProvider"
        @GuardedBy("sInstanceLock")
        private var sInstance: VolleyBasedProvider? = null
        private val sInstanceLock = Any()

        @JvmStatic
        fun getInstance(context: Context, tokenProvider: AsyncTokenProvider, baseUrl: String): VolleyBasedProvider {
            if (sInstance == null) {
                synchronized(sInstanceLock) {
                    if (sInstance == null) sInstance = VolleyBasedProvider(Gson(), Volley.newRequestQueue(context), tokenProvider, baseUrl)
                }
            }
            return sInstance
                    ?: throw IllegalStateException()
        }
    }

}

class InternalApiTokenProvider(
        private val queue: RequestQueue,
        private val deviceId: String,
        private val publishableKey: String,
        private val gson: Gson,
        private val authUrl: String
) : AsyncTokenProvider {
    private var token: String = ""
    companion object { const val TAG = "InternalTokenProvider" }

    override fun getAuthenticationToken(resultHandler: ResultHandler<String>) {
        Log.d(TAG, "Getting Auth Token")
        if (token.isNotEmpty()) {
            Log.d(TAG, "Proceeding with token $token")
            resultHandler.onResult(token)
            return
        }
        Log.d(TAG, "No cached token present, requesting new one")
        queue.add(getInternalTokenRequest(resultHandler))
    }

    private fun getInternalTokenRequest(resultHandler: ResultHandler<String>): GetInternalTokenRequest {
        return GetInternalTokenRequest(gson, deviceId, publishableKey, authUrl,
                Response.Listener {
                    token = it
                    resultHandler.onResult(it)
                },
                Response.ErrorListener {
                    Log.w(TAG, "Authentication request failed with error ${it.networkResponse.data}")
                    resultHandler.onError(it)
                }
        )
    }

    fun refreshToken(queue: RequestQueue, retryCall: () -> Unit) {
        Log.d(TAG, "Refreshing token")
        token = ""
        queue.add(
                GetInternalTokenRequest(gson, deviceId, publishableKey, authUrl,
                        Response.Listener { retryCall() },
                        Response.ErrorListener { retryCall() }
                )
        )
    }
}

