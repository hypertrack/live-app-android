package com.hypertrack.backend

import com.hypertrack.backend.models.Geofence
import com.hypertrack.backend.models.GeofenceLocation
import com.hypertrack.backend.models.GeofenceProperties
import com.hypertrack.backend.models.Point
import org.junit.Assert.*
import org.junit.Test

class GeofenceApiAdapterTest {

    @Test
    fun itShouldReturnNothingIfNoHomeGeofenceSet() {

        var receivedNull = false
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) = callback.onResult(emptySet())
            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {}
            override fun deleteGeofence(geofence_id: String) {}
        })
        geofenceApiAdapter.getHomeGeofenceLocation(object : ResultHandler<GeofenceLocation?> {
            override fun onResult(result: GeofenceLocation?) { if (result == null) receivedNull = true }
            override fun onError(error: Exception) { }
        })

        assertTrue(receivedNull)
    }

    @Test
    fun itShouldReturnHomeIfPresent() {

        var homeLocation : GeofenceLocation? = null
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {
                callback.onResult(setOf(geofenceAt("2020-01-01T20:20:01.010Z", "01")))
            }

            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {}
            override fun deleteGeofence(geofence_id: String) {}
        })
        geofenceApiAdapter.getHomeGeofenceLocation(object : ResultHandler<GeofenceLocation?> {
            override fun onResult(result: GeofenceLocation?) { homeLocation = result }
            override fun onError(error: Exception) { }
        })

        assertNotNull(homeLocation)
        assertEquals(1.0, homeLocation?.longitude)
        assertEquals(2.0, homeLocation?.latitude)
    }

    @Test
    fun itShouldReturnLatestHomeIfMultiplePresent() {

        var homeLocation : GeofenceLocation? = null
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {
                callback.onResult(setOf(
                        geofenceAt("2020-01-01T20:20:01.010Z", "01"),
                        geofenceAt("2020-01-02T20:20:01.010Z", "02", latitude = 3.0, longitude = 4.0)
                ))
            }

            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {}
            override fun deleteGeofence(geofence_id: String) {}
        })
        geofenceApiAdapter.getHomeGeofenceLocation(object : ResultHandler<GeofenceLocation?> {
            override fun onResult(result: GeofenceLocation?) { homeLocation = result }
            override fun onError(error: Exception) { }
        })

        assertNotNull(homeLocation)
        assertEquals(4.0, homeLocation?.longitude)
        assertEquals(3.0, homeLocation?.latitude)
    }

    @Test
    fun itShouldIgnoreNonHomeGeofencesIfPresent() {

        var homeLocation : GeofenceLocation? = null
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {
                callback.onResult(setOf(
                        geofenceAt("2020-01-01T20:20:01.010Z", "01"),
                        geofenceAt("2020-01-02T20:20:01.010Z", "02", latitude = 3.0, longitude = 4.0),
                        geofenceAt("2020-01-03T20:20:01.010Z", "02", metadata = mapOf("name" to "Test"), latitude = 3.0, longitude = 4.0),
                        geofenceAt("2020-01-03T20:42:01.010Z", "02", metadata = emptyMap(), latitude = 3.0, longitude = 4.0)
                ))
            }

            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {}
            override fun deleteGeofence(geofence_id: String) {}
        })
        geofenceApiAdapter.getHomeGeofenceLocation(object : ResultHandler<GeofenceLocation?> {
            override fun onResult(result: GeofenceLocation?) { homeLocation = result }
            override fun onError(error: Exception) { }
        })

        assertNotNull(homeLocation)
        assertEquals(4.0, homeLocation?.longitude)
        assertEquals(3.0, homeLocation?.latitude)
    }

    @Test
    fun itShouldDeleteObsoleteHomeIfMultiplePresent() {

        val deleted = mutableListOf<String>()
        val oldGeofence1 = "99"
        val oldGeofence2 = "01"
        val actualHomeId = "02"
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {
                callback.onResult(setOf(
                        geofenceAt("2019-12-01T20:20:01.010Z", oldGeofence1),
                        geofenceAt("2020-01-01T20:20:01.010Z", oldGeofence2),
                        geofenceAt("2020-01-02T20:20:01.010Z", actualHomeId, latitude = 3.0, longitude = 4.0)
                ))
            }

            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) {}
            override fun deleteGeofence(geofence_id: String) {deleted.add(geofence_id)}
        })
        geofenceApiAdapter.getHomeGeofenceLocation(object : ResultHandler<GeofenceLocation?> {
            override fun onResult(result: GeofenceLocation?) { }
            override fun onError(error: Exception) { }
        })

        assertTrue(deleted.isNotEmpty())
        assertTrue(deleted.contains(oldGeofence1))
        assertTrue(deleted.contains(oldGeofence2))
        assertFalse(deleted.contains(actualHomeId))

    }

    @Test
    fun itShouldDeleteOldHomeGeofencesWhenNewCreated() {

        val deleted = mutableListOf<String>()
        val oldGeofence1 = "99"
        val oldGeofence2 = "01"
        val actualHomeId = "02"
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) {
                callback.onResult(setOf(
                        geofenceAt("2019-12-01T20:20:01.010Z", oldGeofence1),
                        geofenceAt("2020-01-01T20:20:01.010Z", oldGeofence2),
                        geofenceAt("2020-01-02T20:20:01.010Z", actualHomeId, latitude = 3.0, longitude = 4.0)
                ))
            }

            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) { }
            override fun deleteGeofence(geofence_id: String) {deleted.add(geofence_id)}
        })

        geofenceApiAdapter.updateHomeGeofence(GeofenceLocation(42.0, 43.0),
                object : ResultHandler<Void?> {
            override fun onResult(result: Void?) { }
            override fun onError(error: Exception) { }
        })

        assertTrue(deleted.isNotEmpty())
        assertTrue(deleted.contains(oldGeofence1))
        assertTrue(deleted.contains(oldGeofence2))
        assertTrue(deleted.contains(actualHomeId))

    }

    @Test
    fun itShouldCreateNewHomeGeofenceWhenUpdated() {

        mutableListOf<String>()
        var createdProperties : Set<GeofenceProperties>? = null
        val geofenceApiAdapter = GeofenceApiAdapter(object : GeofencesApiProvider {
            override fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>) { createdProperties = geofencesProperties }
            override fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>) = callback.onResult(emptySet())
            override fun deleteGeofence(geofence_id: String) { }
        })

        geofenceApiAdapter.updateHomeGeofence(GeofenceLocation(42.0, 43.0),
                object : ResultHandler<Void?> {
            override fun onResult(result: Void?) { }
            override fun onError(error: Exception) { }
        })

        assertNotNull(createdProperties)
        assertTrue(createdProperties ?. isNotEmpty() ?: false)
        assertTrue(createdProperties?.size == 1)
        val homeParams = createdProperties?.first() ?: throw Error("No geofence parameters in request")

        assertEquals(42.0, homeParams.geometry.latitude, 0.0000001)
        assertEquals(43.0, homeParams.geometry.longitude, 0.0000001)

    }

    private fun geofenceAt(
            createdAt: String, id: String,
            metadata: Map<String, Any> = mapOf("name" to "Home"),
            isArchived: Boolean = false,
            latitude: Double = 2.0,
            longitude: Double = 1.0
    )
            = Geofence(null, createdAt, null, "42", null, id,
            Point(listOf(longitude, latitude)), metadata, 50, isArchived, false)

}