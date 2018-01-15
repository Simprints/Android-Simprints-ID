package com.simprints.id.domain

import com.simprints.id.testUtils.mock
import com.simprints.id.testUtils.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import android.location.Location as AndroidLocation


class LocationTest {

    private val latitude = 42.12467
    private val longitude = 12.4212

    private val latitudeString = "42.12467"
    private val longitudeString = "12.4212"

    private val androidLocation = mockAndroidLocation(latitude, longitude)

    private fun mockAndroidLocation(latitude: Double, longitude: Double): AndroidLocation {
        val mockLocation = mock<AndroidLocation>()
        whenever(mockLocation.latitude).thenReturn(latitude)
        whenever(mockLocation.longitude).thenReturn(longitude)
        return mockLocation
    }

    @Test
    fun test() {
        val location = Location.fromAndroidLocation(androidLocation)
        assertEquals(latitudeString, location.latitude)
        assertEquals(longitudeString, location.longitude)
    }

}
