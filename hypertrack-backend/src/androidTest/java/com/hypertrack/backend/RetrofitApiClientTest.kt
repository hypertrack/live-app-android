package com.hypertrack.backend

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val TAG = "RetrofitApiClientTest"

class RetrofitApiClientTest {
    @Test
    fun itShouldExecuteGetRequestToFetchGeofences() {



        val retrofitApiClient = ApiClient(
                "https://live-app-backend.htprod.hypertrack.com/",
                "https://live-api.htprod.hypertrack.com/authenticate",
"E15E21C3-C942-3FEA-B33B-16A58E291CD0",
"uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw",
                GsonBuilder()
                .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
                        .of(Geometry::class.java)
                        .registerSubtype(Point::class.java, "Point")
                        .registerSubtype(Polygon::class.java, "Polygon"))
                .create()
        )

        val finishedSignal = CountDownLatch(1)
        var testResult: List<Geofence> = emptyList()

        retrofitApiClient.getDeviceGeofences(object : ResultHandler<List<Geofence>> {
            override fun onResult(result: List<Geofence>) {
                Log.d(TAG, "Got geofences list $result")
                testResult = result
                finishedSignal.countDown()
            }

            override fun onError(error: Exception) {
                Log.d(TAG, "Got error $error")
                finishedSignal.countDown()

            }
        })

        finishedSignal.await(30, TimeUnit.SECONDS)
        assert(testResult.isNotEmpty())


    }
}