package com.simprints.id.tools

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth
import com.simprints.id.testtools.TestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

internal class LocationManagerImplTest {

    private lateinit var locationManager: LocationManager
    private lateinit var mockedLocationClient: FusedLocationProviderClient
    private lateinit var mockedLocationTask: Task<Location>
    private var captureCallback = slot<OnCompleteListener<Location>>()
    private var locationResponseTask: Task<Location> = mockk()

    @Before
    fun setUp() {
        mockedLocationClient = mockk()
        mockedLocationTask = mockk()

        every { mockedLocationTask.addOnCompleteListener(capture(captureCallback)) } answers {
            captureCallback.captured.onComplete(locationResponseTask)
            mockk()
        }
        every {
            mockedLocationClient.getCurrentLocation(any(), any())
        } returns mockedLocationTask

        mockkStatic(LocationServices::class)
        every {
            LocationServices.getFusedLocationProviderClient(any<Context>())
        } returns mockedLocationClient

        locationManager = LocationManagerImpl(mockk())
    }


    @Test
    fun `test requestLocation success`() = runBlocking {
        // Given
        val fakeLocation = TestData.buildFakeLocation()
        every { locationResponseTask.isSuccessful } returns true
        every { locationResponseTask.result } returns fakeLocation
        // When
        val flow = locationManager.requestLocation(LocationRequest.create()).take(1)
        // Then
        Truth.assertThat(flow.firstOrNull()).isEqualTo(fakeLocation)
    }

    @Test
    fun `test requestLocation failure`() = runBlocking {
        // Given
        every { locationResponseTask.isSuccessful } returns false
        // When
        val flow = locationManager.requestLocation(LocationRequest.create()).take(1)
        // Then
        Truth.assertThat(flow.firstOrNull()).isEqualTo(null)
    }
}

