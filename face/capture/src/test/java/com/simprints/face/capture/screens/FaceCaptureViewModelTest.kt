package com.simprints.face.capture.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.face.capture.usecases.SaveFaceImageUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.config.store.models.FaceConfiguration.ImageSavingStrategy
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.assertEventReceived
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FaceCaptureViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var faceImageUseCase: SaveFaceImageUseCase

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

    private lateinit var viewModel: FaceCaptureViewModel

    private val faceDetections = listOf<FaceDetection>(
        mockk(relaxed = true) {
            every { id } returns "FAKE_ID"
            every { bitmap } returns mockk {}
        }
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { faceImageUseCase.invoke(any(), any()) } returns null
        every { bitmapToByteArrayUseCase.invoke(any()) } returns byteArrayOf()

        viewModel = FaceCaptureViewModel(
            configManager,
            faceImageUseCase,
            eventReporter,
            bitmapToByteArrayUseCase,
            licenseRepository,
            mockk {
                coEvery { this@mockk().initializer } returns faceBioSdkInitializer
            },
            saveLicenseCheckEvent
        )
    }

    @Test
    fun `Save face detections should not be called when image saving strategy set to NEVER`() {
        coEvery { configManager.getProjectConfiguration().face?.imageSavingStrategy } returns ImageSavingStrategy.NEVER

        viewModel.captureFinished(faceDetections)
        viewModel.flowFinished()
        coVerify(exactly = 0) { faceImageUseCase.invoke(any(), any()) }
    }

    @Test
    fun `Save face detections should be called when image saving strategy set to ONLY_GOOD_SCAN`() {
        coEvery { configManager.getProjectConfiguration().face?.imageSavingStrategy } returns ImageSavingStrategy.ONLY_GOOD_SCAN

        viewModel.captureFinished(faceDetections)
        viewModel.flowFinished()
        coVerify(atLeast = 1) { faceImageUseCase.invoke(any(), any()) }
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
    fun `test initFaceBioSdk should initialize faceBioSdk`() {
        // Given
        val license = "license"
        every { faceBioSdkInitializer.tryInitWithLicense(any(), license) } returns true
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        // When
        viewModel.initFaceBioSdk(mockk())
        // Then
        coVerify { faceBioSdkInitializer.tryInitWithLicense(any(), license) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.VALID)
    }

    @Test
    fun `test initFaceBioSdk should post invalid license when faceBioSdkInitializer returns false`() {
        // Given
        val license = "license"
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2133-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        coEvery { faceBioSdkInitializer.tryInitWithLicense(any(), license) } returns false
        // When
        viewModel.initFaceBioSdk(mockk())
        // Then
        viewModel.invalidLicense.assertEventReceived()
        coVerify { licenseRepository.deleteCachedLicense(Vendor.RankOne) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.ERROR)
    }

    @Test
    fun `test initFaceBioSdk should post invalid license when license is expired`() {
        // Given
        val license = "license"
        coEvery {
            licenseRepository.getCachedLicense(Vendor.RankOne)
        } returns License("2011-12-30T17:32:28Z", license, LicenseVersion("1.0"))
        val licenseStatusSlot = slot<LicenseStatus>()
        coJustRun { saveLicenseCheckEvent(Vendor.RankOne, capture(licenseStatusSlot)) }

        // When
        viewModel.initFaceBioSdk(mockk())
        // Then
        viewModel.invalidLicense.assertEventReceived()
        coVerify { licenseRepository.deleteCachedLicense(Vendor.RankOne) }
        assertThat(licenseStatusSlot.captured).isEqualTo(LicenseStatus.EXPIRED)
    }
}
