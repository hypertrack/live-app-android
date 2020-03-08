package com.hypertrack.trips

import android.content.Context
import android.util.Log
import androidx.annotation.GuardedBy
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.google.gson.Gson


class TripsManager private constructor(
        context: Context,
        private val tokenProvider: AsyncTokenProvider
) {
    private val gson: Gson = Gson()
    private val queue:RequestQueue = Volley.newRequestQueue(context)

    fun createTrip(tripConfig: TripConfig, callback: ResultHandler<ShareableTrip>) {
        Log.i(TAG, "Create trip with config $tripConfig")
        tokenProvider.getAuthenticationToken(object: ResultHandler<String> {

            override fun onResult(result: String) =
                    scheduleAuthenticatedCreateRequest(tripConfig, result, callback)

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
                    scheduleAuthenticatedCompletionRequest(tripId, result, callback)

            override fun onError(error: Exception) = callback.onError(error)

        })
    }

    private fun scheduleAuthenticatedCreateRequest(tripConfig: TripConfig, tokenString: String, callback: ResultHandler<ShareableTrip>) {
        val request = CreateTripRequest(
                tripConfig, gson, tokenString,
                Response.Listener { trip -> callback.onResult(trip) },
                Response.ErrorListener { error -> callback.onError(error as Exception) }
        )
        request.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        Log.d(TAG, "Adding create trip request to queue")
        queue.add(request)
    }

    private fun scheduleAuthenticatedCompletionRequest(tripId: String, tokenString: String, callback: ResultHandler<String>) {
        val request = CompleteTripRequest(
                tripId, tokenString,
                Response.Listener { callback.onResult(tripId) },
                Response.ErrorListener { error -> callback.onError(error as Exception) }
        )
        request.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        Log.d(TAG, "Adding complete trip request to queue")
        queue.add(request)

    }

    companion object {
        const val TAG = "TripsManager"
        @GuardedBy("sInstanceLock")
        private var sInstance: TripsManager? = null
        private val sInstanceLock = Any()

        @JvmStatic
        fun getInstance(context: Context, tokenProvider: AsyncTokenProvider): TripsManager {
            if (sInstance == null) {
                synchronized(sInstanceLock) {
                    if (sInstance == null) sInstance = TripsManager(context, tokenProvider)
                }
            }
            return sInstance?: throw IllegalStateException()
        }
    }

}

interface AsyncTokenProvider { fun getAuthenticationToken(resultHandler: ResultHandler<String>) }

interface ResultHandler<T> {
    fun onResult(result: T)
    fun onError(error: Exception)
}

class ShareableTrip (val shareUrl: String, val embedUrl: String, val tripId: String, val remainingDuration: Int?)

