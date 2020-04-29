package com.hypertrack.backend

import android.content.Context
import android.util.Log
import androidx.annotation.GuardedBy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.net.HttpURLConnection


class PublicKeyAuthorizedBackendProvider(context: Context, publishableKey: String, deviceId: String) {

    val queue = Volley.newRequestQueue(context)
    val gson = Gson()
    val internalApiIssuesTokenProvider = InternalApiTokenProvider(queue, deviceId, publishableKey, gson)
    val backendProvider = BackendProvider(gson, queue, internalApiIssuesTokenProvider)

    fun start(deviceId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "start $deviceId")
        val retryCallback = object : ResultHandler<String> {
            override fun onResult(result: String) = callback.onResult(result)

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<String>(error, callback) {
                        backendProvider.start(deviceId, callback)
                    }
        }
        backendProvider.start(deviceId, retryCallback)
    }

    fun stop(deviceId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "stop $deviceId")
        val retryCallback = object : ResultHandler<String> {
            override fun onResult(result: String) = callback.onResult(result)

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<String>(error, callback) {
                        backendProvider.stop(deviceId, callback)
                    }
        }
        backendProvider.stop(deviceId, retryCallback)

    }

    fun createTrip(tripConfig: TripConfig, callback: ResultHandler<ShareableTrip>) {
        Log.i(TAG, "Creating trip with config $tripConfig")
        val retryCallback = object : ResultHandler<ShareableTrip> {
            override fun onResult(result: ShareableTrip) = callback.onResult(result)

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<ShareableTrip>(error, callback) {
                        backendProvider.createTrip(tripConfig, callback)
                    }
        }
        backendProvider.createTrip(tripConfig, retryCallback)
    }

    fun completeTrip(tripId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "Complete trip $tripId")
        val retryCallback = object : ResultHandler<String> {
            override fun onResult(result: String) = callback.onResult(result)

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<String>(error, callback) {
                        backendProvider.completeTrip(tripId, callback)
                    }
        }
        backendProvider.completeTrip(tripId, retryCallback)

    }

    fun sendGeofenceTransition(deviceId: String, transitionType: String) {
        Log.i(TAG, "Sending geofence transition $transitionType for device $deviceId")
        val retryCallback = object : ResultHandler<Unit> {
            override fun onResult(result: Unit) { }

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<Unit>(error, null) {
                        backendProvider.sendGeofenceTransition(deviceId, transitionType)
                    }
        }
        backendProvider.sendGeofenceTransition(deviceId, transitionType, retryCallback)
    }

    private fun <T> getErrorHandlerWithTokenAutoRefresh(
            error: Exception,
            callback: ResultHandler<T>?,
            retryCall: () -> Unit
    ) {
        when (error) {
            is VolleyError -> {
                if (error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    internalApiIssuesTokenProvider.refreshToken { retryCall() }
                    return
                }
            }
        }
        callback?.onError(error)
    }

    companion object { const val TAG = "PublicKeyAuthorizedBP" }
}

class BackendProvider(
        private val gson: Gson,
        private val queue: RequestQueue,
        private val tokenProvider: AsyncTokenProvider
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

    fun stop(deviceId: String, callback: ResultHandler<String>) {
        Log.i(TAG, "stop $deviceId")
        if (deviceId.isEmpty()) {
            callback.onError(java.lang.Exception("Can't stop with empty deviceId"))
            return
        }

        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleAuthenticatedStopTrackingRequest(deviceId, result, callback)

            override fun onError(error: Exception) = callback.onError(error)

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

    fun sendGeofenceTransition(deviceId: String, transitionType: String, callback: ResultHandler<Unit>? = null) {
        Log.i(TAG, "sendGeofenceTransition  $transitionType")

        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
            override fun onResult(result: String) =
                    scheduleGeofenceEventRequest(deviceId, transitionType, result, callback)

            override fun onError(error: Exception) {
                Log.w(TAG, "Can't fetch token due to error $error")
            }
        })
    }

    private fun scheduleGeofenceEventRequest(deviceId: String, transitionType: String, tokenString: String, callback: ResultHandler<Unit>?) {

        val successListener:Response.Listener<Unit>? = if (callback != null) Response.Listener { callback.onResult(Unit) } else null
        val errorListener:Response.ErrorListener? = if (callback != null) Response.ErrorListener { callback.onError(it) } else null

        val request = GeofenceEventRequest(deviceId, transitionType, tokenString, successListener, errorListener)
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding geofence request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedStartTrackingRequest(deviceId: String, tokenString: String, callback: ResultHandler<String>) {
        val request = StartTrackingRequest(
                deviceId, tokenString,
                Response.Listener { callback.onResult(deviceId) },
                Response.ErrorListener { error -> callback.onError(error) }
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding start request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedStopTrackingRequest(deviceId: String, tokenString: String, callback: ResultHandler<String>) {
        val request = StopTrackingRequest(
                deviceId, tokenString,
                Response.Listener { callback.onResult(deviceId) },
                Response.ErrorListener { error -> callback.onError(error as Exception) }
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding stop request to queue")
        queue.add(request)

    }

    private fun scheduleAuthenticatedCreateTripRequest(tripConfig: TripConfig, tokenString: String, callback: ResultHandler<ShareableTrip>) {
        val request = CreateTripRequest(
                tripConfig, gson, tokenString,
                Response.Listener { trip -> callback.onResult(trip) },
                Response.ErrorListener { error -> callback.onError(error as Exception) }
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding create trip request to queue")
        queue.add(request)
    }

    private fun scheduleAuthenticatedCompletionTripRequest(tripId: String, tokenString: String, callback: ResultHandler<String>) {
        val request = CompleteTripRequest(
                tripId, tokenString,
                Response.Listener { callback.onResult(tripId) },
                Response.ErrorListener { error -> callback.onError(error as Exception) }
        )
        request.retryPolicy = defaultRetryPolicy
        Log.d(TAG, "Adding complete trip request to queue")
        queue.add(request)

    }

    companion object {
        const val TAG = "BackendProvider"
        @GuardedBy("sInstanceLock")
        private var sInstance: BackendProvider? = null
        private val sInstanceLock = Any()

        @JvmStatic
        fun getInstance(context: Context, tokenProvider: AsyncTokenProvider): BackendProvider {
            if (sInstance == null) {
                synchronized(sInstanceLock) {
                    if (sInstance == null) sInstance = BackendProvider(Gson(), Volley.newRequestQueue(context), tokenProvider)
                }
            }
            return sInstance ?: throw IllegalStateException()
        }
    }

}

class InternalApiTokenProvider(
        private val queue: RequestQueue,
        private val deviceId: String,
        private val publishableKey: String,
        private val gson: Gson
) : AsyncTokenProvider {
    private var token: String = ""
    override fun getAuthenticationToken(resultHandler: ResultHandler<String>) {
        if (token.isNotEmpty()) {
            resultHandler.onResult(token)
            return
        }
        queue.add(getInternalTokenRequest(resultHandler))
    }

    private fun getInternalTokenRequest(resultHandler: ResultHandler<String>): GetInternalTokenRequest {
        return GetInternalTokenRequest(gson, deviceId, publishableKey,
                Response.Listener {
                    token = it
                    resultHandler.onResult(it)
                },
                Response.ErrorListener { resultHandler.onError(it) }
        )
    }

    fun refreshToken(function: (String) -> Unit) {
        TODO("Not yet implemented")
    }
}

interface AsyncTokenProvider {
    fun getAuthenticationToken(resultHandler: ResultHandler<String>)
}

interface ResultHandler<T> {
    fun onResult(result: T)
    fun onError(error: Exception)
}

class ShareableTrip(val shareUrl: String, val embedUrl: String, val tripId: String, val remainingDuration: Int?)

