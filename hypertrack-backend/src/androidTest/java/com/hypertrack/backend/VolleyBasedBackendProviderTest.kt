package com.hypertrack.backend

import android.content.Context
import android.util.Log
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.hypertrack.backend.models.ShareableTrip
import com.hypertrack.backend.models.TripConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class VolleyBasedBackendProviderTest {

    private lateinit var appContext : Context
    private lateinit var backendProvider: AbstractBackendProvider


    @Before
    fun setup() {

        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        backendProvider = HybridBackendProvider.getInstance(context = appContext, publishableKey = PUBLISHABLE_KEY, deviceID = DEVICE_ID)
    }

    @Test @LargeTest
    fun test0010ItShouldCreateTripWithGivenDestination() {

        val request = TripConfig.Builder()
                .setDestinationLatitude(35.120995)
                .setDestinationLongitude(47.84918)
                .setDeviceId(DEVICE_ID)
                .build()

        val latch = CountDownLatch(1)
        backendProvider.createTrip(request, object : ResultHandler<ShareableTrip> {
            override fun onResult(result: ShareableTrip) {
                Log.i(TAG,"Got shareable trip ${result.tripId}")
                assertNotNull(result)
                testTrip = result
                latch.countDown()
            }

            override fun onError(error: Exception) {
                Log.e(TAG, "Failed with error $error")
                latch.countDown()
                throw error
            }
        })

        latch.await(TEST_TIMEOUT, TimeUnit.SECONDS)
        assertNotNull(testTrip)
        assertNotNull(testTrip?.tripId)
        Log.i(TAG, "Created trip ${testTrip?.tripId}")
        assertNotNull(testTrip?.shareUrl)
        Log.i(TAG, "Share url ${testTrip?.shareUrl}")
        assertNotNull(testTrip?.embedUrl)
        Log.i(TAG, "Embed url ${testTrip?.embedUrl}")


    }


    @Test @LargeTest
    fun test0090ItShouldCompleteTripWhenRequested() {
        assertNotNull(testTrip)
        Log.d(TAG,"Scheduling completion for trip ${testTrip?.tripId}")

        val requestFinishedSignal = CountDownLatch(1)

        var completedTripId:String? = null

        testTrip?.let {  backendProvider.completeTrip(it.tripId, object : ResultHandler<String> {
            override fun onResult(result: String) {
                completedTripId = result
                requestFinishedSignal.countDown()
            }

            override fun onError(error: Exception) {
                requestFinishedSignal.countDown()
                throw error
            }

        })}

        requestFinishedSignal.await(TEST_TIMEOUT, TimeUnit.SECONDS)
        assertNotNull(completedTripId)
        assertEquals(testTrip?.tripId, completedTripId)
    }

    @Test
    fun test0050ItShouldGetDeeplinkWhenRequested() {
        Log.d(TAG,"Getting deeplink")
        val requestFinishedSignal = CountDownLatch(1)
        var deeplink = ""
        backendProvider.getInviteLink(object : ResultHandler<String> {
            override fun onResult(result: String) {
                deeplink = result
                requestFinishedSignal.countDown()
            }

            override fun onError(error: Exception) {
                requestFinishedSignal.countDown()
            }
        })
        requestFinishedSignal.await(TEST_TIMEOUT, TimeUnit.SECONDS)
        assertTrue(deeplink.isNotEmpty())

    }

    @Test
    fun test0060ItShouldGetDeeplinkWhenRequested() {
        Log.d(TAG,"Getting accountEmail")
        val requestFinishedSignal = CountDownLatch(1)
        var accountName = ""
        backendProvider.getAccountName(object : ResultHandler<String> {
            override fun onResult(result: String) {
                accountName = result
                requestFinishedSignal.countDown()
            }

            override fun onError(error: Exception) {
                requestFinishedSignal.countDown()
            }
        })
        requestFinishedSignal.await(TEST_TIMEOUT, TimeUnit.SECONDS)
        assertTrue(accountName.isNotEmpty())

    }

    companion object {
        var testTrip: ShareableTrip? = null
        const val DEVICE_ID = "E15E21C3-C942-3FEA-B33B-16A58E291CD0"
        const val PUBLISHABLE_KEY = "uvIAA8xJANxUxDgINOX62-LINLuLeymS6JbGieJ9PegAPITcr9fgUpROpfSMdL9kv-qFjl17NeAuBHse8Qu9sw"
        const val TAG = "BackendProviderTest"
        const val TEST_TIMEOUT = 10L
    }
}