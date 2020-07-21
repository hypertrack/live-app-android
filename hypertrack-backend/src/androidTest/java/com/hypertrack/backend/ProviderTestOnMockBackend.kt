package com.hypertrack.backend

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Test
import java.net.HttpURLConnection

class ProviderTestOnMockBackend {

    val stubTokenResponse = """{"access_token": "stub_token", "expires_in": 86400, "token_type": "Basic"}"""
    val stubGeofencesResponse = """{
                                      "links": { "next": null },
                                      "pagination_token": null,
                                      "data": [
                                        {
                                          "delete_at": null,
                                          "created_at": "2020-07-21T13:46:51.232225+00:00",
                                          "device_id": "E15E21C3-C942-3FEA-B33B-16A58E291CD0",
                                          "archived": null,
                                          "metadata": { "device_geofence": true, "name": "Home" },
                                          "geometry": {
                                            "coordinates": [ 12.3456, 42.4321 ],
                                            "type": "Point"
                                          },
                                          "single_use": false,
                                          "geofence_id": "66847d04-6373-4530-a0fd-dcfbba13013e",
                                          "radius": 50
                                        },
                                        {
                                          "radius": 50,
                                          "geofence_id": "dccf2463-49ee-4233-a6ca-3dda6b24c622",
                                          "single_use": false,
                                          "metadata": { "device_geofence": true, "name": "Home" },
                                          "geometry": {
                                            "type": "Point",
                                            "coordinates": [ 12.1234, 42.1234 ]
                                          },
                                          "archived": null,
                                          "device_id": "E15E21C3-C942-3FEA-B33B-16A58E291CD0",
                                          "created_at": "2020-07-21T13:46:50.879470+00:00",
                                          "delete_at": null
                                        }
                                      ]
                                    }"""
    @Test
    fun getGeofencesTest() {

        val mockWebServer = MockWebServer()
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest?): MockResponse {
                when (request?.path) {
                    "authenticate" -> return MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(stubTokenResponse)
                            "client/geofences/" -> return MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(stubGeofencesResponse)
                }
                return MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
            }
        }
        mockWebServer.setDispatcher(dispatcher)
        mockWebServer.start()

        val backendProvider : AbstractBackendProvider = PublicKeyAuthorizedBackendProvider(
                InstrumentationRegistry.getInstrumentation().targetContext,
"some_publishable_key", "42",
                mockWebServer.url("").toString(),
                mockWebServer.url("authenticate").toString()
        )

        backendProvider.getHomeGeofence(
                object : ResultHandler<GeofenceLocation> {
                    override fun onResult(result: GeofenceLocation) {
                        assert(result.latitude == 42.4321)
                        assert(result.longitude == 12.3456)
                    }

                    override fun onError(error: Exception): Unit = throw error
                }
        )

        val request = mockWebServer.takeRequest()
        assert(request.path == "authenticate")
        val geofencesRequest = mockWebServer.takeRequest()
        assert(geofencesRequest.path == "client/geofences/")


        mockWebServer.shutdown()


    }
}
