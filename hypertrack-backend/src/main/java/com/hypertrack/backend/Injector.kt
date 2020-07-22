package com.hypertrack.backend

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.hypertrack.backend.models.Geometry
import com.hypertrack.backend.models.Point
import com.hypertrack.backend.models.Polygon
import okhttp3.OkHttpClient

object Injector {

    const val baseUrl = "https://live-app-backend.htprod.hypertrack.com/"
    const val authUrl = "https://live-api.htprod.hypertrack.com/authenticate"

    val gson: Gson by lazy {
        GsonBuilder()
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
                        .of(Geometry::class.java)
                        .registerSubtype(Point::class.java, "Point")
                        .registerSubtype(Polygon::class.java, "Polygon"))
                .create()
    }

    fun getBasicAccessTokenProvider(deviceId: String, publishableKey: String) : AccessTokenRepository
            = BasicAuthAccessTokenRepository(authUrl, deviceId, publishableKey)


    fun getRetrofitBasedGeofencesApiClient(deviceId: String, publishableKey: String)
            = RetrofitGeofencesApiClient(
            baseUrl, deviceId, gson, getBasicAccessTokenProvider(deviceId, publishableKey)
    )

    fun okHttpClient(authorizer: AccessTokenRepository): OkHttpClient {
        return OkHttpClient.Builder()
                .authenticator(AccessTokenAuthenticator(authorizer))
                .addInterceptor(AccessTokenInterceptor(authorizer))
                .addInterceptor(UserAgentInterceptor())
                .build()
    }

    fun getHomeManagementApiProvider(deviceId: String, publishableKey: String): HomeManagementApi {
        return GeofenceApiAdapter(
                getRetrofitBasedGeofencesApiClient(deviceId, publishableKey)
        )
    }
}