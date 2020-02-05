package com.hypertrack.trips

import androidx.annotation.FloatRange
import java.lang.NullPointerException

class TripConfig internal constructor(
        val latitude: Double,
        val longitude: Double,
        val deviceId: String
) {

    class Builder {
        private var destinationLatitude: Double? = null
        private var destinationLongitude: Double? = null
        private var deviceId : String? = null

        /**
         * @param latitude of trip's destination. Negative values are for southern hemisphere.
         * Unless both latitude and longitude are set values are ignored.
         * @see .setDestinationLongitude
         */
        fun setDestinationLatitude(@FloatRange(from = -90.0, to = 90.0) latitude: Double): Builder {
            destinationLatitude = latitude
            return this
        }

        /**
         * @param longitude of trip's destination. Negative values are for western hemisphere.
         * Unless both latitude and longitude are set values are ignored.
         * @see .setDestinationLatitude
         */
        fun setDestinationLongitude(@FloatRange(from = -180.0, to = 180.0) longitude: Double): Builder {
            destinationLongitude = longitude
            return this
        }

        fun setDeviceId(deviceId: String): Builder {
            this.deviceId = deviceId
            return this
        }

        fun build(): TripConfig {
            requireNotNull(destinationLatitude)
            requireNotNull(destinationLongitude)
            requireNotNull(deviceId)
            return TripConfig(
                    destinationLatitude ?: throw NullPointerException("latitude can't be null"),
                    destinationLongitude ?: throw NullPointerException("longitude can't be null"),
                    deviceId ?: throw NullPointerException("device id can't be null")
            )
        }
    }

}