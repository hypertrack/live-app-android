package com.hypertrack.backend

import android.content.Context
import android.util.Log
import androidx.annotation.GuardedBy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson


class BackendProvider private constructor(
        context: Context,
        private val tokenProvider: AsyncTokenProvider
) {
    private val gson: Gson = Gson()
    private val queue: RequestQueue = Volley.newRequestQueue(context)
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

    fun sendGeofenceTransition(transitionType: String) {
        Log.i(TAG, "sendGeofenceTransition  $transitionType")

//        tokenProvider.getAuthenticationToken(object : ResultHandler<String> {
//            override fun onResult(result: String) =
//                    scheduleAuthenticatedCompletionTripRequest(transitionType, result, callback)
//
//            override fun onError(error: Exception) = callback.onError(error)

//        })
    }

    private fun scheduleAuthenticatedStartTrackingRequest(deviceId: String, tokenString: String, callback: ResultHandler<String>) {
        val request = StartTrackingRequest(
                deviceId, tokenString,
                Response.Listener { callback.onResult(deviceId) },
                Response.ErrorListener { error -> callback.onError(error as Exception) }
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
                    if (sInstance == null) sInstance = BackendProvider(context, tokenProvider)
                }
            }
            return sInstance ?: throw IllegalStateException()
        }
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

