package com.simprints.feature.setup.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import com.simprints.feature.setup.LocationStore
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseState
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

    @MockK
    private lateinit var saveLicenseCheckEvent: SaveLicenseCheckEventUseCase

    private val configManager = mockk<ConfigManager>()
    private lateinit var viewModel: SetupViewModel

    private val deviceID = "deviceID"
    private val projectId = "projectId"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { authStore.signedInProjectId } returns projectId
        viewModel =
            SetupViewModel(
                locationStore,
                configManager,
                licenseRepository,
                deviceID,
                authStore,
                saveLicenseCheckEvent,
            )
    }

    @Test
    fun `should request location permission if collectLocation is enabled`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns true

        // When
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertHasValue()
    }

    @Test
    fun `should not request location permission if collectLocation is disabled`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration().general.collectLocation } returns false
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.commCare
        } returns mockk()

        // when
        viewModel.start()

        // Then
        viewModel.requestLocationPermission.test().assertNoValue()
    }

    @Test
    fun `should call locationStore collectLocationInBackground() if location permission is granted`() = runTest {
        // Given
        justRun { locationStore.collectLocationInBackground() }
        coEvery { configManager.getProjectConfiguration() } returns mockk<ProjectConfiguration>()
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.commCare
        } returns mockk()

        // when
        viewModel.locationPermissionCheckDone(granted = true)

        // Then
        verify { locationStore.collectLocationInBackground() }
    }

    @Test
    fun `should not call locationStore collectLocationInBackground() if location permission is not granted`() = runTest {
        // Given
        justRun { locationStore.collectLocationInBackground() }
        coEvery { configManager.getProjectConfiguration() } returns mockk<ProjectConfiguration>()
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.commCare
        } returns mockk()

        // when
        viewModel.locationPermissionCheckDone(granted = false)

        // Then
        verify(exactly = 0) { locationStore.collectLocationInBackground() }
    }

    @Test
    fun `should request CommCare permission if needed when location permission is granted`() = runTest {
        // Given
        justRun { locationStore.collectLocationInBackground() }
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.commCare
        } returns mockk()

        // When
        viewModel.locationPermissionCheckDone(true)

        // Then
        viewModel.requestCommCarePermission.test().assertHasValue()
    }

    @Test
    fun `should request CommCare permission if needed when location permission is not granted`() = runTest {
        // Given
        justRun { locationStore.collectLocationInBackground() }
        coEvery {
            configManager
                .getProjectConfiguration()
                .synchronization.down.commCare
        } returns mockk()

        // When
        viewModel.locationPermissionCheckDone(false)

        // Then
        viewModel.requestCommCarePermission.test().assertHasValue()
    }

    @Test
    fun `should download required licenses`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(
                    GeneralConfiguration.Modality.FINGERPRINT,
                    GeneralConfiguration.Modality.FACE,
                )
            }
            every { fingerprint } returns mockk {
                every { allowedSDKs } returns listOf(FingerprintConfiguration.BioSdk.NEC)
                every { nec?.version } returns "1"
            }
            every { face } returns mockk {
                every { allowedSDKs } returns listOf(FaceConfiguration.BioSdk.RANK_ONE)
                every { rankOne?.version } returns "1"
            }
        }
        every {
            licenseRepository.getLicenseStates(any(), any(), any(), any())
        } returns listOf(LicenseState.FinishedWithSuccess(License("expirationDate", "license", LicenseVersion.UNLIMITED))).asFlow()

        // When
        viewModel.notificationPermissionCheckDone()

        // Then
        verify(exactly = 2) { licenseRepository.getLicenseStates(any(), any(), any(), LicenseVersion("1")) }
        viewModel.overallSetupResult.test().assertValue(true)
    }

    @Test
    fun `should download required licenses with unlimited versions`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(
                    GeneralConfiguration.Modality.FINGERPRINT,
                    GeneralConfiguration.Modality.FACE,
                )
                every { fingerprint } returns mockk {
                    every { allowedSDKs } returns listOf(FingerprintConfiguration.BioSdk.NEC)
                    every { nec?.version } returns null
                }
                every { face } returns mockk {
                    every { allowedSDKs } returns listOf(FaceConfiguration.BioSdk.RANK_ONE)
                    every { rankOne?.version } returns null
                }
            }
        }
        every {
            licenseRepository.getLicenseStates(any(), any(), any(), LicenseVersion.UNLIMITED)
        } returns listOf(LicenseState.FinishedWithSuccess(License("expirationDate", "license", LicenseVersion.UNLIMITED))).asFlow()

        // When
        viewModel.notificationPermissionCheckDone()

        // Then
        verify(exactly = 2) { licenseRepository.getLicenseStates(any(), any(), any(), any()) }
        viewModel.overallSetupResult.test().assertValue(true)
    }

    @Test
    fun `should not download required licenses if there are no required licenses`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(
                    GeneralConfiguration.Modality.FINGERPRINT,
                    GeneralConfiguration.Modality.FACE,
                )
                every { fingerprint } returns mockk {
                    every { allowedSDKs } returns listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER)
                }
                every { face } returns mockk {
                    every { allowedSDKs } returns listOf(FaceConfiguration.BioSdk.SIM_FACE)
                }
            }
        }

        // When
        viewModel.notificationPermissionCheckDone()

        // Then
        viewModel.overallSetupResult.test().assertValue(true)
    }

    @Test
    fun `should fail if any license fails`() = runTest {
        // Given
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { modalities } returns listOf(
                    GeneralConfiguration.Modality.FINGERPRINT,
                    GeneralConfiguration.Modality.FACE,
                )
            }
            every { fingerprint } returns mockk {
                every { allowedSDKs } returns listOf(FingerprintConfiguration.BioSdk.NEC)
                every { nec?.version } returns ""
            }
            every { face } returns mockk {
                every { allowedSDKs } returns listOf(FaceConfiguration.BioSdk.RANK_ONE)
                every { rankOne?.version } returns ""
            }
        }
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, LicenseStatus.MISSING) }
        every {
            licenseRepository.getLicenseStates(any(), any(), Vendor.Nec, any())
        } returns listOf(LicenseState.FinishedWithSuccess(License("expirationDate", "", LicenseVersion.UNLIMITED))).asFlow()
        every {
            licenseRepository.getLicenseStates(any(), any(), Vendor.RankOne, any())
        } returns listOf(LicenseState.FinishedWithError("123")).asFlow()

        // When
        viewModel.notificationPermissionCheckDone()

        // Then
        coVerify { saveLicenseCheckEvent(Vendor.RankOne, LicenseStatus.MISSING) }
        viewModel.overallSetupResult.test().assertValue(false)
    }
}
