package com.simprints.feature.setup.location

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class LocationManagerTest {
    @MockK
    private lateinit var locationManager: LocationManager

    @MockK
    private lateinit var mockedLocationClient: FusedLocationProviderClient

    @MockK
    private lateinit var mockedLocationTask: Task<Location>

    @MockK
    private lateinit var locationResponseTask: Task<Location>

    private var captureCallback = slot<OnCompleteListener<Location>>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockedLocationTask.addOnCompleteListener(capture(captureCallback)) } answers {
            captureCallback.captured.onComplete(locationResponseTask)
            mockk()
        }
        every {
            mockedLocationClient.getCurrentLocation(any<Int>(), any())
        } returns mockedLocationTask

        mockkStatic(LocationServices::class)
        every {
            LocationServices.getFusedLocationProviderClient(any<Context>())
        } returns mockedLocationClient

        locationManager = LocationManager(mockk())
    }

    @Test
    fun `test requestLocation success`() = runTest {
        // Given
        val fakeLocation = TestLocationData.buildFakeLocation()
        every { locationResponseTask.isSuccessful } returns true
        every { locationResponseTask.result } returns fakeLocation
        // When
        val flow = locationManager.requestLocation(LocationRequest.create()).take(1)
        // Then
        Truth.assertThat(flow.firstOrNull()).isEqualTo(fakeLocation)
    }

    @Test
    fun `test requestLocation failure`() = runTest {
        // Given
        every { locationResponseTask.isSuccessful } returns false
        // When
        val flow = locationManager.requestLocation(LocationRequest.create()).take(1)
        // Then
        Truth.assertThat(flow.firstOrNull()).isEqualTo(null)
    }
}
