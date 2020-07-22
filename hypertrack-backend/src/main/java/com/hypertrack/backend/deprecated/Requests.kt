package com.hypertrack.backend.deprecated

import android.os.Build
import android.util.Base64
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.hypertrack.backend.BuildConfig
import com.hypertrack.backend.models.GeofenceLocation
import com.hypertrack.backend.models.GeofenceResponse
import com.hypertrack.backend.models.ShareableTrip
import com.hypertrack.backend.models.TripConfig
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

private const val TAG = "Requests"

class StartTrackingRequest(
        deviceId: String,
        tokenString: String,
        responseListener: Response.Listener<Void>,
        errorListener: Response.ErrorListener,
        baseUrl: String
) : LiveAppBackendRequest<Void>(
        tokenString, "${baseUrl}client/devices/$deviceId/start", "",
        responseListener, errorListener
) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<Void> {
        response?.let { if (isSuccessFamilyStatus(it)) return Response.success(null, null) }
        Log.e(TAG, "Got status code ${response?.statusCode}, body ${response?.toString()}")
        return Response.error(VolleyError(response))
    }

}

class StopTrackingRequest(
        deviceId: String,
        tokenString: String,
        responseListener: Response.Listener<Void>,
        errorListener: Response.ErrorListener,
        baseUrl: String
) : LiveAppBackendRequest<Void>(
        tokenString, "${baseUrl}client/devices/$deviceId/stop", "",
        responseListener, errorListener
) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<Void> {
        response?.let { if (isSuccessFamilyStatus(it)) return Response.success(null, null) }
        Log.e(TAG, "Got status code ${response?.statusCode}, body ${response?.toString()}")
        return Response.error(VolleyError(response))
    }

}

class CreateGeofencesRequest(
        location: GeofenceLocation,
        deviceId: String,
        private val gson: Gson,
        tokenString: String,
        responseListener: Response.Listener<String>,
        errorListener: Response.ErrorListener,
        baseUrl: String
) : LiveAppBackendRequest<String>(
        tokenString, "${baseUrl}client/geofences/?device_id=$deviceId", "{\"geofences\":[{\"radius\":50,\"geometry\":{\"type\":\"Point\",\"coordinates\":[${location.longitude},${location.latitude}]},\"metadata\":{\"name\":\"Home\"}}],\"device_id\":\"$deviceId\"}",
        responseListener, errorListener
) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        response?.let {
            // Expired token etc.
            if (!isSuccessFamilyStatus(response)) {
                return Response.error(VolleyError(response))
            }
            try {
                val responseBody = String(it.data, Charset.forName(HttpHeaderParser.parseCharset(it.headers, Charsets.UTF_8.name())))
                if (responseBody.isEmpty()) {
                    return Response.error(VolleyError("Can't parse geofence from empty response"))
                }
                val result = gson.fromJson<List<GeofenceResponse>>(responseBody,
                        object : TypeToken<List<GeofenceResponse>>(){}.type)
                result?.let {geofences ->
                    if (geofences.isNotEmpty())
                        return Response.success(geofences[0].geofenceId, HttpHeaderParser.parseCacheHeaders(response))
                }
            } catch (error: UnsupportedEncodingException) {
                return Response.error(VolleyError(error))
            } catch (error: JsonSyntaxException) {
                return Response.error(VolleyError(error))
            }

        }
        Log.e(TAG, "Got status code ${response?.statusCode}, body ${response?.toString()}")
        return Response.error(VolleyError(response))
    }

}

class CreateTripRequest(tripConfig: TripConfig, private val gson: Gson, tokenString: String,
                        responseListener: Response.Listener<ShareableTrip>, errorListener: Response.ErrorListener, baseUrl: String
) :
        LiveAppBackendRequest<ShareableTrip>(tokenString, "${baseUrl}client/trips/",
                tripConfig.getRequestBody(), responseListener, errorListener
        ) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<ShareableTrip> {
        response?.let {

            // Expired token etc.
            if (!isSuccessFamilyStatus(response)) {
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
                            ShareableTrip(trip.views.shareUrl, trip.views.embedUrl, trip.tripId, trip.estimate?.route?.remainingDuration),
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
}

class CompleteTripRequest(tripId: String, tokenString: String,
                          responseListener: Response.Listener<Void>,
                          errorListener: Response.ErrorListener,
                          baseUrl: String) :
        LiveAppBackendRequest<Void>(tokenString,
                "${baseUrl}client/trips/$tripId/complete", "",
                responseListener, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<Void> {
        response?.let { if (isSuccessFamilyStatus(it)) return Response.success(null, null) }
        Log.e(TAG, "Got status code ${response?.statusCode}, body ${response?.toString()} url $url")
        return Response.error(VolleyError(response))
    }
}

class GetDeeplinkRequest(tokenString: String, responseListener: Response.Listener<String>,
                         errorListener: Response.ErrorListener,
                         baseUrl: String) :
        LiveAppBackendRequest<String>(tokenString,
                "${baseUrl}client/deep_link/live", "",
                responseListener, errorListener, Method.GET) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        response?.let { if (isSuccessFamilyStatus(it)) return Response.success(String(it.data), null) }
        Log.e(TAG, "Got status code ${response?.statusCode}, body ${response?.toString()} url $url")
        return Response.error(VolleyError(response))
    }
}

class GetAccountEmailRequest(tokenString: String, responseListener: Response.Listener<String>,
                             errorListener: Response.ErrorListener,
                             baseUrl: String) :
        LiveAppBackendRequest<String>(tokenString,
                "${baseUrl}client/account_name/", "",
                responseListener, errorListener, Method.GET) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        response?.let { if (isSuccessFamilyStatus(it)) return Response.success(String(it.data), null) }
        Log.e(TAG, "Got status code ${response?.statusCode}, body ${response?.toString()} url $url")
        return Response.error(VolleyError(response))
    }
}


class GetInternalTokenRequest(
        private val gson: Gson,
        deviceId: String,
        private val publishableKey: String,
        authUrl: String,
        responseListener: Response.Listener<String>,
        errorListener: Response.ErrorListener
) : JsonRequest<String>(
        Method.POST, authUrl,
        """{"device_id":"$deviceId","scope":"generation"}""",
        responseListener, errorListener
) {
    override fun getHeaders(): MutableMap<String, String> {
        val genericHeaders = super.getHeaders()
        val headers = HashMap<String, String>(genericHeaders.size + 2)
        headers["User-Agent"] = getUserAgent()
        headers["Authorization"] = "Basic ${Base64.encodeToString("$publishableKey:".toByteArray(), Base64.NO_WRAP)}"
        Log.d(TAG, "Authentication request headers are $headers")
        return headers
    }

    override fun parseNetworkResponse(networkResponse: NetworkResponse?): Response<String> {
        networkResponse?.let {
            response ->
            Log.d(TAG, "Got auth response $response")
            if (isSuccessFamilyStatus(response)) {

                try {
                    val responseBody = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers, Charsets.UTF_8.name())))
                    if (responseBody.isEmpty()) {
                        return Response.error(VolleyError("Can't get token from empty response"))
                    }
                    val tokenResponse = gson.fromJson<TokenResponse>(responseBody, TokenResponse::class.java)
                    tokenResponse?.token?.let { token ->
                        Log.d(TAG, "Got token $token")
                        return Response.success(token, null)
                    }
                } catch (error: UnsupportedEncodingException) {
                    return Response.error(VolleyError(error))
                } catch (error: JsonSyntaxException) {
                    return Response.error(VolleyError(error))
                }
            }
        }
        return Response.error(VolleyError("No response received"))
    }
}

fun isSuccessFamilyStatus(networkResponse: NetworkResponse) = networkResponse.statusCode / 100 == 2
fun getUserAgent() = "LiveApp/${BuildConfig.VERSION_NAME} Volley/1.1.1 Android/${Build.VERSION.RELEASE}"

abstract class LiveAppBackendRequest<T>(private val tokenString: String, requestUrl: String, requestBody: String, responseListener: Response.Listener<T>?,
                                        errorListener: Response.ErrorListener?, requestMethod:Int = Method.POST
) : JsonRequest<T>(requestMethod, requestUrl, requestBody, responseListener, errorListener
) {

    override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
        volleyError?.networkResponse?.let { Log.e(TAG, "Got error from url $url data ${String(it.data)}") }
        return super.parseNetworkError(volleyError)
    }

    override fun getHeaders(): MutableMap<String, String> {
        val defaultHeaders = super.getHeaders()
        val headers = HashMap<String, String>(defaultHeaders.size + 2)
        headers.putAll(defaultHeaders)
        headers["Authorization"] = "Bearer $tokenString"
        headers["User-Agent"] = getUserAgent()
        Log.d(TAG, "Executing LiveBackend request with headers $headers")
        return headers
    }
}

private data class TokenResponse(
        @SerializedName("access_token") val token: String?
)

private data class Views(
        @SerializedName("share_url") val shareUrl: String,
        @SerializedName("embed_url") val embedUrl: String
)

private data class Trip(
        @SerializedName("trip_id") val tripId: String,
        @SerializedName("views") val views: Views,
        @SerializedName("estimate") val estimate: Estimate?
)

private data class Estimate(
        @SerializedName("route") val route: Route?
)

private data class Route(
        @SerializedName("remaining_duration") val remainingDuration: Int?
)