package com.hypertrack.backend

import android.util.Log
import com.hypertrack.backend.models.Geofence
import com.hypertrack.backend.models.GeofenceProperties
import com.hypertrack.backend.models.Point
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val TAG = "RetrofitApiClientTest"
private const val DEVICE_ID = "E15E21C3-C942-3FEA-B33B-16A58E291CD0"
private const val PUBLISHABLE_KEY = "uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw"
class RetrofitApiClientTest {

    lateinit var apiClient: RetrofitGeofencesApiClient

    @Before
    fun setUp() { apiClient = buildApiClient() }

    @Test
    fun itShouldExecuteGetRequestToFetchGeofences() {

        val finishedSignal = CountDownLatch(1)
        var testResult: Set<Geofence> = emptySet()

        apiClient.getDeviceGeofences(object : ResultHandler<Set<Geofence>> {
            override fun onResult(result: Set<Geofence>) {
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

        val activeGeofence = testResult.filter { it.archived == null }.last()

        val deleteSignal = CountDownLatch(1)

        apiClient.deleteGeofence(activeGeofence.geofence_id)
        deleteSignal.await(10, TimeUnit.SECONDS)


    }

    @Test
    fun createGeofenceTest() {

        val createSignal = CountDownLatch(1)
        var geofence: Geofence? = null

        apiClient.createGeofences(
                setOf(
                        GeofenceProperties(Point(listOf(35.1206527856364, 47.850388852921)), mapOf("name" to "Test 02"), 50)
                ),
                object : ResultHandler<Set<Geofence>> {
                    override fun onResult(result: Set<Geofence>) {
                        Log.d(TAG, "Got result from create geofence API $result")
                        geofence = result.first()
                        createSignal.countDown()

                    }

                    override fun onError(error: Exception) {
                        Log.d(TAG, "Got error creating geofence $error")
                        createSignal.countDown()
                    }
                }
        )
        createSignal.await(30, TimeUnit.SECONDS)
        geofence ?: throw Error("No geofence was created")

    }

    private fun buildApiClient(): RetrofitGeofencesApiClient {
        return Injector.getRetrofitBasedGeofencesApiClient(DEVICE_ID, PUBLISHABLE_KEY)
    }


}