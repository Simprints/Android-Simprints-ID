package com.simprints.face.capture.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.BitmapToByteArrayUseCase
import com.simprints.face.capture.usecases.SaveFaceImageUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FaceConfiguration.ImageSavingStrategy
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
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
        )
    }

    @Test
    fun `Save face detections should not be called when image saving strategy set to NEVER`() = runTest {
        coEvery { configManager.getProjectConfiguration().face?.imageSavingStrategy } returns ImageSavingStrategy.NEVER

        viewModel.captureFinished(faceDetections)
        viewModel.flowFinished()
        coVerify(exactly = 0) { faceImageUseCase.invoke(any(), any()) }
    }

    @Test
    fun `Save face detections should be called when image saving strategy set to ONLY_GOO_SCAN`() = runTest {
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
        viewModel.addOnboardingComplete(0L)

        verify { eventReporter.addOnboardingCompleteEvent(any()) }
    }

    @Test
    fun `Saves event on capture confirmation`() {
        viewModel.addCaptureConfirmationAction(0L, true)

        verify { eventReporter.addCaptureConfirmationEvent(any(), any()) }
    }
}
