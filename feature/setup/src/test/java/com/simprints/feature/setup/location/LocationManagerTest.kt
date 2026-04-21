package com.simprints.feature.setup.location

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LocationManagerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @MockK
    private lateinit var locationManager: LocationManager

    @MockK
    private lateinit var mockedLocationClient: FusedLocationProviderClient

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var config: ProjectConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(LocationServices::class)
        every {
            LocationServices.getFusedLocationProviderClient(any<Context>())
        } returns mockedLocationClient

        coEvery { configRepository.getProjectConfiguration() } returns config
        every { config.custom } returns emptyMap()

        locationManager = LocationManager(mockk(), configRepository)
    }

    @Test
    fun `test requestLocation success both locations`() = runTest {
        // Given
        val fakeLastLocation = TestLocationData.buildFakeLocation().apply {
            latitude = 10.0
            longitude = 10.0
            time = 1000L
        }
        val fakeCurrentLocation = TestLocationData.buildFakeLocation().apply {
            latitude = 20.0
            longitude = 20.0
        }

        every { mockedLocationClient.lastLocation } returns Tasks.forResult(fakeLastLocation)
        every { mockedLocationClient.getCurrentLocation(any<Int>(), any()) } returns Tasks.forResult(fakeCurrentLocation)

        // When
        val flow = locationManager.requestLocation()

        // Then
        val results = flow.toList()
        assertThat(results).hasSize(2)

        assertThat(results[0]?.latitude).isEqualTo(10.0)
        assertThat(results[0]?.longitude).isEqualTo(10.0)
        assertThat(results[0]?.lastLocationTime).isEqualTo(1000L)

        assertThat(results[1]?.latitude).isEqualTo(20.0)
        assertThat(results[1]?.longitude).isEqualTo(20.0)
        assertThat(results[1]?.lastLocationTime).isNull()
    }

    @Test
    fun `test requestLocation success only current location`() = runTest {
        // Given
        val fakeCurrentLocation = TestLocationData.buildFakeLocation().apply {
            latitude = 30.0
            longitude = 30.0
        }

        every { mockedLocationClient.lastLocation } returns Tasks.forResult(null)
        every { mockedLocationClient.getCurrentLocation(any<Int>(), any()) } returns Tasks.forResult(fakeCurrentLocation)

        // When
        val flow = locationManager.requestLocation()

        // Then
        val results = flow.toList()
        assertThat(results).hasSize(1)

        assertThat(results[0]?.latitude).isEqualTo(30.0)
        assertThat(results[0]?.longitude).isEqualTo(30.0)
    }

    @Test
    fun `test requestLocation success only last location`() = runTest {
        // Given
        val fakeLastLocation = TestLocationData.buildFakeLocation().apply {
            latitude = 40.0
            longitude = 40.0
        }

        every { mockedLocationClient.lastLocation } returns Tasks.forResult(fakeLastLocation)
        every { mockedLocationClient.getCurrentLocation(any<Int>(), any()) } returns Tasks.forResult(null)

        // When
        val flow = locationManager.requestLocation()

        // Then
        val results = flow.toList()
        assertThat(results).hasSize(1)

        assertThat(results[0]?.latitude).isEqualTo(40.0)
        assertThat(results[0]?.longitude).isEqualTo(40.0)
    }

    @Test
    fun `test requestLocation failure both locations absent`() = runTest {
        // Given
        every { mockedLocationClient.lastLocation } returns Tasks.forResult(null)
        every { mockedLocationClient.getCurrentLocation(any<Int>(), any()) } returns Tasks.forResult(null)

        // When
        val flow = locationManager.requestLocation()

        // Then
        val results = flow.toList()
        assertThat(results).isEmpty()
    }

    @Test
    fun `uses high accuracy if no custom configuration`() = runTest {
        // Given
        every { mockedLocationClient.lastLocation } returns Tasks.forResult(null)
        every { mockedLocationClient.getCurrentLocation(any<Int>(), any()) } returns Tasks.forResult(null)

        // When
        locationManager.requestLocation().toList()

        // Then
        coVerify(exactly = 1) { mockedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, any()) }
    }

    @Test
    fun `uses balanced accuracy if has custom configuration`() = runTest {
        // Given
        clearMocks(config)
        every { config.custom } returns mapOf("useBalancedLocationAccuracy" to JsonPrimitive(true))

        every { mockedLocationClient.lastLocation } returns Tasks.forResult(null)
        every { mockedLocationClient.getCurrentLocation(any<Int>(), any()) } returns Tasks.forResult(null)

        // When
        locationManager.requestLocation().toList()

        // Then
        coVerify { mockedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, any()) }
    }
}
