package com.simprints.feature.setup.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.simprints.feature.setup.LocationStore
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.Vendor
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class SetupViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var licenseRepository: LicenseRepository

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var locationStore: LocationStore

    private val configRepository = mockk<ConfigRepository>()
    private lateinit var viewModel: SetupViewModel

    private val deviceID = "deviceID"
    private val projectId = "projectId"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { authStore.signedInProjectId } returns projectId
        viewModel =
            SetupViewModel(locationStore, configRepository, licenseRepository, deviceID, authStore)
    }

    @Test
    fun `should request location permission if collectLocation is enabled`() = runTest {
        // Given
        coEvery { configRepository.getProjectConfiguration().general.collectLocation } returns true

        // When
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertHasValue()

    }

    @Test
    fun `should not request location permission if collectLocation is disabled`() = runTest {
        // Given
        coEvery { configRepository.getProjectConfiguration().general.collectLocation } returns false

        // when
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertNoValue()

    }


    @Test
    fun `should call locationStore collectLocationInBackground if collectLocation is called`() =
        runTest {
            // Given
            justRun { locationStore.collectLocationInBackground() }

            // when
            viewModel.collectLocation()

            // Then
            verify { locationStore.collectLocationInBackground() }

        }

    @Test
    fun `should request notification permission if collectLocation is disabled`() = runTest {
        // Given
        coEvery { configRepository.getProjectConfiguration().general.collectLocation } returns false

        // When
        viewModel.start()

        // Then
        viewModel.requestNotificationPermission.test().assertHasValue()

    }

    @Test
    fun `should not request notification permission yet if collectLocation is enabled`() = runTest {
        // Given
        coEvery { configRepository.getProjectConfiguration().general.collectLocation } returns true

        // When
        viewModel.start()

        // Then
        viewModel.requestNotificationPermission.test().assertNoValue()

    }

    @Test
    fun `should request notification permission on command`() = runTest {
        // When
        viewModel.requestNotificationsPermission()

        // Then
        viewModel.requestNotificationPermission.test().assertHasValue()
    }

    @Test
    fun `should download required licenses`() = runTest {
        // Given
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(
                    GeneralConfiguration.Modality.FINGERPRINT,
                    GeneralConfiguration.Modality.FACE
                )
            }
            every { fingerprint } returns mockk {
                every { allowedSDKs } returns listOf(FingerprintConfiguration.BioSdk.NEC)
            }
        }
        every {
            licenseRepository.getLicenseStates(any(), any(), any())
        } returns listOf(LicenseState.FinishedWithSuccess("license")).asFlow()

        // When
        viewModel.downloadRequiredLicenses()

        // Then
        verify(exactly = 2) { licenseRepository.getLicenseStates(any(), any(), any()) }
        viewModel.overallSetupResult.test().assertValue(true)
    }

    @Test
    fun `should fail if any license fails`() = runTest {
        // Given
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(
                    GeneralConfiguration.Modality.FINGERPRINT,
                    GeneralConfiguration.Modality.FACE
                )
            }
            every { fingerprint } returns mockk {
                every { allowedSDKs } returns listOf(FingerprintConfiguration.BioSdk.NEC)
            }
        }
        every {
            licenseRepository.getLicenseStates(any(), any(), Vendor.NEC_FINGERPRINT_VENDOR)
        } returns listOf(LicenseState.FinishedWithSuccess("")).asFlow()
        every {
            licenseRepository.getLicenseStates(any(), any(), Vendor.RANK_ONE_FACE_VENDOR)
        } returns listOf(LicenseState.FinishedWithError("123")).asFlow()

        // When
        viewModel.downloadRequiredLicenses()

        // Then
        viewModel.overallSetupResult.test().assertValue(false)
    }
}
