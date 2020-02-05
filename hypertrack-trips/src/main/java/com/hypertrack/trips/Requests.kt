package com.hypertrack.trips

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.nio.charset.Charset

private const val liveAppBackendTripsEndpoint = "https://live-app-backend.htprod.hypertrack.com/trips/"

class CreateTripRequest(
        tripConfig: TripConfig,
        private val gson: Gson,
        private val tokenString: String,
        responseListener: Response.Listener<ShareableTrip>,
        errorListener: Response.ErrorListener
) :
        JsonRequest<ShareableTrip>(
                Method.POST,
                liveAppBackendTripsEndpoint,
                tripConfig.getRequestBody(),
                responseListener,
                errorListener
        ) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<ShareableTrip> {
        response?.let {

            // Expired token etc.
            if (response.statusCode != HttpURLConnection.HTTP_CREATED) {
                return Response.error(VolleyError(response))
            }

            try {
                val responseBody = String(it.data, Charset.forName(HttpHeaderParser.parseCharset(it.headers, Charsets.UTF_8.name())))
                if (responseBody.isEmpty()) {
                    return Response.error(VolleyError("Can't create trip from empty response"))
                }
                val parsedTrip = gson.fromJson<Trip>(responseBody, Trip::class.java)
                parsedTrip?.let { trip ->
                    return Response.success(
                            ShareableTrip(trip.views.shareUrl, trip.views.embedUrl, trip.tripId),
                            HttpHeaderParser.parseCacheHeaders(response)
                    )
                }
            } catch (error: UnsupportedEncodingException) {
                return Response.error(VolleyError(error))
            } catch (error: JsonSyntaxException) {
                return Response.error(VolleyError(error))
            }
        }
        return Response.error(VolleyError("Can't create shareable trip without response"))
    }

    override fun getHeaders(): MutableMap<String, String> {
        val defaultHeaders = super.getHeaders()
        val headers = HashMap<String, String>(defaultHeaders.size + 1)
        headers.putAll(defaultHeaders)
        headers["Authorization"] = "Bearer $tokenString"
        return headers
    }
}

class CompleteTripRequest(
        tripId: String,
        private val tokenString: String,
        responseListener: Response.Listener<Void>,
        errorListener: Response.ErrorListener
) : JsonRequest<Void>(
        Method.POST, liveAppBackendTripsEndpoint + "$tripId/complete", "",
        responseListener, errorListener
) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<Void> {
        response?.let { if (it.statusCode == HttpURLConnection.HTTP_ACCEPTED) return Response.success(null, null) }
        return Response.error(VolleyError(response))
    }

    override fun getHeaders(): MutableMap<String, String> {
        val defaultHeaders = super.getHeaders()
        val headers = HashMap<String, String>(defaultHeaders.size + 1)
        headers.putAll(defaultHeaders)
        headers["Authorization"] = "Bearer $tokenString"
        return headers
    }
}

private data class Views(
        @SerializedName("share_url") val shareUrl: String,
        @SerializedName("embed_url") val embedUrl: String
)

private data class Trip(
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("views") val views: Views
)