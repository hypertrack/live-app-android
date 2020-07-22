package com.hypertrack.backend

import com.google.gson.Gson
import com.hypertrack.backend.models.Geofence
import com.hypertrack.backend.models.GeofenceParams
import com.hypertrack.backend.models.GeofenceProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface GeofencesApiInterface {

    @GET("client/devices/{device_id}/geofences")
    suspend fun getDeviceGeofences(@Path("device_id")deviceId : String) : Set<Geofence>

    @POST("client/devices/{device_id}/geofences")
    suspend fun createGeofences(@Path("device_id")deviceId : String, @Body params: GeofenceParams) : Set<Geofence>

    @DELETE("client/geofences/{geofence_id}")
    suspend fun deleteGeofence(@Path("geofence_id")geofence_id: String) : Response<Unit>



}

class RetrofitGeofencesApiClient(baseUrl: String, private val deviceId: String, gson: Gson, authorizer: AccessTokenRepository)
    : GeofencesApiProvider {

    val api: GeofencesApiInterface = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(Injector.okHttpClient(authorizer))
            .build().create(GeofencesApiInterface::class.java)

    override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {

        GlobalScope.launch(Dispatchers.Default) {
            try {
                val geofences = api.getDeviceGeofences(deviceId)
                callback.onResult(geofences)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val params = GeofenceParams(geofencesProperties, deviceId)
                val createdGeofenses = api.createGeofences(deviceId, params)
                callback.onResult(createdGeofenses)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    override fun deleteGeofence(geofence_id: String) {
        GlobalScope.launch(Dispatchers.Default) { api.deleteGeofence(geofence_id) }
    }
}

