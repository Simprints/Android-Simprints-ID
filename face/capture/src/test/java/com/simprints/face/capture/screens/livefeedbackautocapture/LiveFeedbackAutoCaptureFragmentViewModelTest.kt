package com.simprints.face.capture.screens.livefeedbackautocapture

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class LiveFeedbackAutoCaptureFragmentViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var faceDetector: FaceDetector

    @MockK
    lateinit var frame: Bitmap

    @MockK
    lateinit var previewFrame: Bitmap

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var eventReporter: SimpleCaptureEventReporter

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var viewModel: LiveFeedbackAutoCaptureFragmentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProjectConfiguration().face?.qualityThreshold } returns QUALITY_THRESHOLD
        coEvery { configManager.getProjectConfiguration().experimental().singleQualityFallbackRequired } returns false
        every { timeHelper.now() } returnsMany (0..100L).map { Timestamp(it) }
        justRun { previewFrame.recycle() }
        val resolveFaceBioSdkUseCase = mockk<ResolveFaceBioSdkUseCase> {
            coEvery { this@mockk.invoke() } returns mockk {
                every { detector } returns faceDetector
            }
        }

        viewModel = LiveFeedbackAutoCaptureFragmentViewModel(
            resolveFaceBioSdkUseCase,
            configManager,
            eventReporter,
            timeHelper,
        )
    }


    @Test
    fun `Do not start capture if valid quality face detected but before start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.process(frame)

        assertThat(currentDetection.observedValues)
            .isEmpty()
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.NOT_STARTED)
    }

    @Test
    fun `Do not start capture if valid quality face detected but capture held off`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.holdOffCapture()
        viewModel.process(frame)

        assertThat(currentDetection.observedValues)
            .isEmpty()
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.NOT_STARTED)
    }

    @Test
    fun `Do not start capture if no valid quality face detected after start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace(quality = -2f)
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)

        assertThat(currentDetection.observedValues.last()?.status)
            .isEqualTo(FaceDetection.Status.BAD_QUALITY)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.NOT_STARTED)
    }

    @Test
    fun `Start capture if valid quality face detected when start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)

        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.CAPTURING)
    }

    @Test
    fun `Proceed with capture if capture attempted to be held off after it started`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)
        viewModel.holdOffCapture()
        viewModel.process(frame)

        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.CAPTURING)
    }

    @Test
    fun `Start capture if valid quality face detected later than start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returnsMany listOf(
            getFace(quality = -2f),
            getFace(),
        )
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)
        viewModel.process(frame)

        assertThat(currentDetection.observedValues.first()?.status)
            .isEqualTo(FaceDetection.Status.BAD_QUALITY)
        assertThat(capturingState.observedValues.first())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.NOT_STARTED)
        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.CAPTURING)
    }

    @Test
    fun `Process fallback image when valid face correctly but not started capture`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()

        viewModel.initCapture(1, 0)
        viewModel.process(frame) // a fallback image frame before the preparation delay elapses
        viewModel.startCapture()
        viewModel.process(frame)

        val currentDetection = viewModel.currentDetection.testObserver()
        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)

        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `Process valid face correctly`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        val currentDetection = viewModel.currentDetection.testObserver()
        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)

        coVerify { eventReporter.addCaptureEvents(any(), any(), any(), any()) }
    }

    @Test
    fun `Process invalid faces correctly`() = runTest {
        val smallFace: Face = getFace(Rect(0, 0, 30, 30))
        val bigFace: Face = getFace(Rect(0, 0, 80, 80))
        val yawedFace: Face = getFace(yaw = 45f)
        val rolledFace: Face = getFace(roll = 45f)
        val badQuality: Face = getFace(quality = -2f)
        val noFace = null

        every { faceDetector.analyze(frame) } returnsMany listOf(
            smallFace,
            bigFace,
            yawedFace,
            rolledFace,
            badQuality,
            noFace,
        )

        val detections = viewModel.currentDetection.testObserver()
        viewModel.initCapture(2, 0)
        viewModel.startCapture()

        viewModel.process(frame)
        viewModel.process(frame)
        viewModel.process(frame)
        viewModel.process(frame)
        viewModel.process(frame)
        viewModel.process(frame)

        detections.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.TOOFAR)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.TOOCLOSE)
            assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.OFFYAW)
            assertThat(it[3]?.status).isEqualTo(FaceDetection.Status.OFFROLL)
            assertThat(it[4]?.status).isEqualTo(FaceDetection.Status.BAD_QUALITY)
            assertThat(it[5]?.status).isEqualTo(FaceDetection.Status.NOFACE)
        }

        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any()) }
    }

    @Test
    fun `Process invalid faces after single fallback correctly`() = runTest {
        val validFace: Face = getFace()
        val badQuality: Face = getFace(quality = -2f)

        coEvery { configManager.getProjectConfiguration().experimental().singleQualityFallbackRequired } returns true

        every { faceDetector.analyze(frame) } returnsMany listOf(
            badQuality, // not a fallback image due to bad quality
            validFace, // fallback image
            validFace, // 1st capture
            badQuality, // 2nd capture
        )

        val detections = viewModel.currentDetection.testObserver()
        viewModel.initCapture(1, 0)
        // fallback image frames before the preparation delay elapses
        viewModel.process(frame)
        viewModel.process(frame)
        viewModel.startCapture()
        viewModel.process(frame)
        viewModel.process(frame)

        detections.observedValues.let {
            // fallback image frame wasn't observed during preparation delay
            assertThat(it[0]?.hasValidStatus()).isEqualTo(true)
            assertThat(it[1]?.hasValidStatus()).isEqualTo(true)
        }
    }

    @Test
    fun `Use default imaging duration when not configured`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        coEvery { configManager.getProjectConfiguration().experimental().faceAutoCaptureImagingDurationMillis } returns AUTO_CAPTURE_IMAGING_DURATION_MS
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.CAPTURING)

        advanceTimeBy(1)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.FINISHED)
    }

    @Test
    fun `Use custom imaging duration when provided in config`() = runTest {
        val configDuration = 5000
        coEvery { faceDetector.analyze(frame) } returns getFace()
        coEvery { configManager.getProjectConfiguration().custom } returns mapOf("faceAutoCaptureImagingDurationMillis" to configDuration)
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initCapture(1, 0)
        viewModel.startCapture()
        viewModel.process(frame)
        advanceTimeBy(configDuration.toLong())
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.CAPTURING)

        advanceTimeBy(1)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.FINISHED)
    }

    @Test
    fun `Save all valid captures without fallback image`() = runTest {
        val validFace: Face = getFace()
        every { faceDetector.analyze(frame) } returns validFace

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()
        val samplesToKeep = 2
        viewModel.initCapture(samplesToKeep, 0)
        viewModel.process(frame) // won't be observed during the preparation phase
        viewModel.startCapture()
        (1..100).forEach {
            viewModel.process(frame)
        }
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        currentDetectionObserver.observedValues.let {
            // 1st frame wasn't observed during preparation delay
            assertThat(it[0]?.hasValidStatus()).isEqualTo(true)
            assertThat(it[1]?.hasValidStatus()).isEqualTo(true)
        }

        capturingStateObserver.observedValues.let {
            assertThat(it[0]).isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.NOT_STARTED)
            assertThat(it[1]).isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.CAPTURING)
            assertThat(it[2]).isEqualTo(LiveFeedbackAutoCaptureFragmentViewModel.CapturingState.FINISHED)
        }

        assertThat(viewModel.userCaptures.size).isEqualTo(samplesToKeep)
        viewModel.userCaptures.let {
            with(it[0]) {
                assertThat(hasValidStatus()).isEqualTo(true)
                assertThat(face).isEqualTo(validFace)
                assertThat(isFallback).isEqualTo(false)
            }

            assertThat(it[1].isFallback).isEqualTo(false)
        }

        with(viewModel.sortedQualifyingCaptures) {
            assertThat(size).isEqualTo(samplesToKeep)
            assertThat(get(0).face).isEqualTo(validFace)
            assertThat(get(0).isFallback).isEqualTo(false)
            assertThat(get(1).face).isEqualTo(validFace)
            assertThat(get(1).isFallback).isEqualTo(false)
        }

        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
        coVerify(exactly = 3) { eventReporter.addCaptureEvents(any(), any(), any(), any()) }
    }

    private fun getFace(
        rect: Rect = Rect(0, 0, 60, 60),
        quality: Float = 1f,
        yaw: Float = 0f,
        roll: Float = 0f,
    ) = Face(100, 100, rect, yaw, roll, quality, Random.nextBytes(20), "format")

    companion object {
        private const val QUALITY_THRESHOLD = -1f
        private const val AUTO_CAPTURE_IMAGING_DURATION_MS = 3000L
    }
}
