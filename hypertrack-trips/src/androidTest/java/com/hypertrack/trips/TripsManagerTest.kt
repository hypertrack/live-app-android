package com.hypertrack.trips

import android.content.Context
import android.util.Log
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.Tokens
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TripsManagerTest {

    lateinit var awsMobileClient: AWSMobileClient
    lateinit var appContext : Context
    lateinit var awsAsyncTokenProvider: AsyncTokenProvider
    lateinit var tripManager: TripsManager


    @Before
    fun setup() {
        val countDownLatch = CountDownLatch(1)
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        awsMobileClient = AWSMobileClient.getInstance()
                awsMobileClient.initialize(appContext, object : Callback<UserStateDetails> {
                    override fun onResult(result: UserStateDetails?) {
                        Log.i(TAG, "Aws mobile client initialized")
                        signIn(countDownLatch)
                    }

                    override fun onError(e: java.lang.Exception?) {
                        Log.e(TAG, "AWS Client initialization failed with exception $e")
                        countDownLatch.countDown()
                    }
                })
        Log.i(TAG, "AWS client Setup finished")
        countDownLatch.await(TEST_TIMEOUT, TimeUnit.SECONDS)

        awsAsyncTokenProvider = object : AsyncTokenProvider {
            override fun getAuthenticationToken(resultHandler: ResultHandler<String>) {
                AWSMobileClient.getInstance().getTokens(object :Callback<Tokens> {
                    override fun onResult(result: Tokens?) {
                        val tokenString = result?.idToken?.tokenString
                        tokenString?.let {
                            resultHandler.onResult(tokenString)
                            return
                        }
                        fail("Can't fetch AWS token")
                    }

                    override fun onError(e: java.lang.Exception?) {
                        fail("Can't fetch AWS token $e")
                    }
                })
            }
        }
        tripManager = TripsManager.getInstance(appContext, awsAsyncTokenProvider)
    }

    private fun signIn(countDownLatch: CountDownLatch) {
        awsMobileClient.signIn("team@hypertrack.com", "Hyp3rTr@ck321",
                null, object : Callback<SignInResult> {
            override fun onResult(result: SignInResult?) {
                Log.i(TAG, "AWS signin finished with result $result")
                countDownLatch.countDown()
            }

            override fun onError(e: java.lang.Exception?) {
                Log.e(TAG, "AWS signing failed with exception $e")
                countDownLatch.countDown()
            }

        })
    }

    @Test @LargeTest
    fun test0010ItShouldCreateTripWithGivenDestination() {

        val request = TripConfig.Builder()
                .setDestinationLatitude(35.120995)
                .setDestinationLongitude(47.84918)
                .setDeviceId("E15E21C3-C942-3FEA-B33B-16A58E291CD0")
                .build()

        val latch = CountDownLatch(1)
        tripManager.createTrip(request, object : ResultHandler<ShareableTrip> {
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

        testTrip?.let {  tripManager.completeTrip(it.tripId, object : ResultHandler<String> {
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

    companion object {
        var testTrip: ShareableTrip? = null
        const val TAG = "TripsManagerTest"
        const val TEST_TIMEOUT = 10L
    }
}