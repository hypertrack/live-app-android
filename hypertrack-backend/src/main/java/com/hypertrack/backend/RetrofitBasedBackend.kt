package com.hypertrack.backend

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface ApiInterface {

    @GET("client/devices/{device_id}/geofences")
    suspend fun getDeviceGeofences(@Path("device_id")deviceId : String) : Set<Geofence>

    @POST("client/devices/{device_id}/geofences")
    suspend fun createGeofences(@Path("device_id")deviceId : String, @Body params: GeofenceParams) : Set<Geofence>

    @DELETE("client/geofences/{geofence_id}")
    suspend fun deleteGeofence(@Path("geofence_id")geofence_id: String) : Response<Unit>



}

class ApiClient(
        baseUrl:String,
        authUrl: String,
        private val deviceId: String,
        publishableKey: String,
        gson: Gson
) {

    private val accessTokenRepository = BasicAuthAccessTokenRepository(authUrl, deviceId, publishableKey)

    val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(
                    OkHttpClient.Builder()
                            .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                            .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
                            .addInterceptor(UserAgentInterceptor())
                            .build()
            )
            .build().create(ApiInterface::class.java)

    fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {

        GlobalScope.launch(Dispatchers.Default) {
            try {
                val geofences = api.getDeviceGeofences(deviceId)
                callback.onResult(geofences)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {
        GlobalScope.launch {
            try {
                val params = GeofenceParams(geofencesProperties, deviceId)
                val createdGeofenses = api.createGeofences(deviceId, params)
                callback.onResult(createdGeofenses)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    fun deleteGeofence(geofence_id: String) {
        GlobalScope.launch { api.deleteGeofence(geofence_id) }
    }
}

