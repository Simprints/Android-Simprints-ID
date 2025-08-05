package com.simprints.face.capture.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.face.capture.usecases.IsUsingAutoCaptureUseCase
import com.simprints.face.capture.usecases.SaveFaceSampleUseCase
import com.simprints.face.capture.usecases.ShouldShowInstructionsScreenUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration.ImageSavingStrategy
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseState
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.assertEventNotReceived
import com.simprints.testtools.common.livedata.assertEventReceived
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.*
import io.mockk.impl.annotations.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FaceCaptureViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var authStore: AuthStore

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var faceImageUseCase: SaveFaceSampleUseCase

    @MockK
    private lateinit var eventReporter: SimpleCaptureEventReporter

    @MockK
    private lateinit var bitmapToByteArrayUseCase: BitmapToByteArrayUseCase

    @RelaxedMockK
    private lateinit var faceBioSdkInitializer: FaceBioSdkInitializer

    @MockK
    private lateinit var licenseRepository: LicenseRepository

    @MockK
    private lateinit var saveLicenseCheckEvent: SaveLicenseCheckEventUseCase

    @MockK
    private lateinit var shouldShowInstructionsScreen: ShouldShowInstructionsScreenUseCase

    private lateinit var viewModel: FaceCaptureViewModel

    private val faceDetections = listOf<FaceDetection>(
        mockk(relaxed = true) {
            every { id } returns "FAKE_ID"
            every { bitmap } returns mockk {}
        },
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { faceImageUseCase.invoke(any(), any()) } returns null
        every { bitmapToByteArrayUseCase.invoke(any()) } returns byteArrayOf()
        every { authStore.signedInProjectId } returns "projectId"

        viewModel = FaceCaptureViewModel(
            authStore,
            configManager,
            faceImageUseCase,
            eventReporter,
            bitmapToByteArrayUseCase,
            licenseRepository,
            mockk {
                coEvery { this@mockk(any()).initializer } returns faceBioSdkInitializer
            },
            saveLicenseCheckEvent,
            shouldShowInstructionsScreen,
            "deviceId",
        )
    }

    @Test
    fun `Save face detections should not be called when image saving strategy set to NEVER`() {
        coEvery {
            configManager
                .getProjectConfiguration()
                .face
                ?.getSdkConfiguration(any())
                ?.imageSavingStrategy
        } returns ImageSavingStrategy.NEVER

        viewModel.captureFinished(faceDetections)
        viewModel.flowFinished()
        coVerify(exactly = 0) { faceImageUseCase.invoke(any(), any()) }
    }

    @Test
    fun `Save face detections should be called when image saving strategy set to ONLY_GOOD_SCAN`() {
        coEvery {
            configManager
                .getProjectConfiguration()
                .face
                ?.getSdkConfiguration(any())
                ?.imageSavingStrategy
        } returns ImageSavingStrategy.ONLY_GOOD_SCAN

        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.SIM_FACE)
        viewModel.captureFinished(faceDetections)
        viewModel.flowFinished()
        coVerify(atLeast = 1) { faceImageUseCase.invoke(any(), any()) }
    }

    @Test
    fun `Save biometric reference creation when flow finishes`() {
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.SIM_FACE)
        viewModel.captureFinished(faceDetections)
        viewModel.flowFinished()
        coVerify(atLeast = 1) {
            eventReporter.addBiometricReferenceCreationEvents(any(), any())
        }
    }

    @Test
    fun `Recapture requests clears capture list`() {
        viewModel.captureFinished(faceDetections)
        viewModel.recapture()

        assertThat(viewModel.recaptureEvent.getOrAwaitValue()).isNotNull()
        assertThat(viewModel.getSampleDetection()).isNull()
    }

    @Test
    fun `Requests exit form on back press`() {
        viewModel.handleBackButton()

        assertThat(viewModel.exitFormEvent.getOrAwaitValue()).isNotNull()
    }

    @Test
    fun `Requests error on back press`() {
        viewModel.submitError(Exception())

        assertThat(viewModel.unexpectedErrorEvent.getOrAwaitValue()).isNotNull()
    }

    @Test
    fun `Saves event on complete onboarding`() {
        viewModel.addOnboardingComplete(Timestamp(0L))

        verify { eventReporter.addOnboardingCompleteEvent(any()) }
    }

    @Test
    fun `Saves event on capture confirmation`() {
        viewModel.addCaptureConfirmationAction(Timestamp(0L), true)

        verify { eventReporter.addCaptureConfirmationEvent(any(), any()) }
    }

    @Test
    fun `test initFaceBioSdk should not check licence for SIM_FACE`() {
        // Given
        every { faceBioSdkInitializer.tryInitWithLicense(any(), any()) } returns true

        // When
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.SIM_FACE)

        // Then
        coVerify(exactly = 1) { faceBioSdkInitializer.tryInitWithLicense(any(), eq("")) }
        coVerify(exactly = 0) {
            licenseRepository.getCachedLicense(any())
            saveLicenseCheckEvent(any(), any())
        }
    }

    @Test
    fun `test initFaceBioSdk should initialize faceBioSdk only once`() {
        // Given
        val license = "license"
        every { faceBioSdkInitializer.tryInitWithLicense(any(), license) } returns true
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        // When
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)

        // Then
        coVerify(exactly = 1) { faceBioSdkInitializer.tryInitWithLicense(any(), license) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.VALID)
    }

    @Test
    fun `test initFaceBioSdk should post invalid license when faceBioSdkInitializer always returns false`() {
        // Given
        val license = "license"
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }
        // Downloading not expired licence
        coEvery {
            licenseRepository.redownloadLicence(any(), any(), any(), any())
        } returns flowOf(
            LicenseState.Started,
            LicenseState.Downloading,
            LicenseState.FinishedWithSuccess(License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0"))),
        )
        coEvery { faceBioSdkInitializer.tryInitWithLicense(any(), license) } returns false

        // When
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)
        // Then
        viewModel.invalidLicense.assertEventReceived()
        coVerify { licenseRepository.redownloadLicence(any(), any(), any(), any()) }
        coVerify { licenseRepository.deleteCachedLicense(Vendor.RankOne) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.ERROR)
    }

    @Test
    fun `test initFaceBioSdk should try re-downloading licence when expired`() {
        // Given
        val license = "license"
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2011-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        coEvery {
            licenseRepository.redownloadLicence(any(), any(), any(), any())
        } returns flowOf(
            LicenseState.Started,
            LicenseState.Downloading,
            LicenseState.FinishedWithSuccess(
                License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0")),
            ),
        )
        every { faceBioSdkInitializer.tryInitWithLicense(any(), license) } returns true
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        // When
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)

        // Then
        viewModel.invalidLicense.assertEventNotReceived()
        coVerify { licenseRepository.redownloadLicence(any(), any(), Vendor.RankOne, any()) }
        coVerify { faceBioSdkInitializer.tryInitWithLicense(any(), license) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.VALID)
    }

    @Test
    fun `test initFaceBioSdk should try re-downloading licence when initially invalid`() {
        // Given
        val license = "license"
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        coEvery {
            licenseRepository.redownloadLicence(any(), any(), any(), any())
        } returns flowOf(
            LicenseState.Started,
            LicenseState.Downloading,
            LicenseState.FinishedWithSuccess(
                License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0")),
            ),
        )
        every { faceBioSdkInitializer.tryInitWithLicense(any(), license) } returnsMany listOf(false, true)

        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        // When
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)

        // Then
        viewModel.invalidLicense.assertEventNotReceived()
        coVerify { licenseRepository.redownloadLicence(any(), any(), Vendor.RankOne, any()) }
        coVerify(exactly = 2) { faceBioSdkInitializer.tryInitWithLicense(any(), license) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.VALID)
    }

    @Test
    fun `preparation instructions screen should be set to showing according to its use case`() {
        // Given
        coEvery { shouldShowInstructionsScreen() } returns true

        // When
        val isShowing = viewModel.shouldShowInstructionsScreen()

        // Then
        assertThat(isShowing).isTrue()
    }

    @Test
    fun `test initFaceBioSdk should return error when licednce re-download fails`() {
        // Given
        val license = "license"
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2011-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        coEvery {
            licenseRepository.redownloadLicence(any(), any(), any(), any())
        } returns flowOf(
            LicenseState.Started,
            LicenseState.Downloading,
            LicenseState.FinishedWithError("error"),
        )

        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        // When
        viewModel.initFaceBioSdk(mockk(), FaceConfiguration.BioSdk.RANK_ONE)

        // Then
        viewModel.invalidLicense.assertEventReceived()
        coVerify { licenseRepository.deleteCachedLicense(Vendor.RankOne) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.MISSING)
    }
}
