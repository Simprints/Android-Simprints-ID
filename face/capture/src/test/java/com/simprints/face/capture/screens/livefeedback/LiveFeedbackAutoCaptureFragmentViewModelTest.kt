package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.usecases.GetSpoofCheckConfigurationUseCase
import com.simprints.face.capture.usecases.IsUsingAutoCaptureUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
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
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var eventReporter: SimpleCaptureEventReporter

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var isUsingAutoCapture: IsUsingAutoCaptureUseCase

    @MockK
    private lateinit var getSpoofCheckConfiguration: GetSpoofCheckConfigurationUseCase
    private lateinit var viewModel: LiveFeedbackFragmentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.getSdkConfiguration(any())
                ?.qualityThreshold
        } returns QUALITY_THRESHOLD
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns FaceConfiguration.SpoofCheckConfiguration.DISABLED
        coEvery {
            configRepository.getProjectConfiguration().custom
        } returns mapOf("singleQualityFallbackRequired" to JsonPrimitive(false))

        every { timeHelper.now() } returnsMany (0..100L).map { Timestamp(it) }
        justRun { previewFrame.recycle() }
        val resolveFaceBioSdkUseCase = mockk<ResolveFaceBioSdkUseCase> {
            coEvery { this@mockk.invoke(any()) } returns mockk {
                every { detector } returns faceDetector
            }
        }

        viewModel = LiveFeedbackFragmentViewModel(
            resolveFaceBioSdkUseCase,
            configRepository,
            eventReporter,
            timeHelper,
            isUsingAutoCapture,
            getSpoofCheckConfiguration,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Do not start capture if valid quality face detected but before start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)

        assertThat(currentDetection.observedValues)
            .isEmpty()
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
    }

    @Test
    fun `Do not start capture if valid quality face detected but capture held off`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.holdOffAutoCapture()
        viewModel.process(frame, frame)

        assertThat(currentDetection.observedValues)
            .isEmpty()
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
    }

    @Test
    fun `Start capture if valid quality face detected when start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
    }

    @Test
    fun `Proceed with capture if capture attempted to be held off after it started`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        viewModel.holdOffAutoCapture()
        viewModel.process(frame, frame)

        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
    }

    @Test
    fun `Start capture if valid quality face detected later than start capture clicked`() = runTest {
        coEvery { faceDetector.analyze(frame) } returnsMany listOf(
            getFace(quality = -2f),
            getFace(),
        )
        val currentDetection = viewModel.currentDetection.testObserver()
        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        assertThat(currentDetection.observedValues.first()?.status)
            .isEqualTo(FaceDetection.Status.VALID)
        assertThat(capturingState.observedValues.first())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
        assertThat(currentDetection.observedValues.last()?.hasValidStatus())
            .isEqualTo(true)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
    }

    @Test
    fun `Process fallback image when valid face correctly but not started capture`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame) // a fallback image frame before the preparation delay elapses
        viewModel.startCapture()
        viewModel.process(frame, frame)

        val currentDetection = viewModel.currentDetection.testObserver()
        assertThat(currentDetection.observedValues.last()?.hasValidStatus()).isEqualTo(true)

        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `Process valid face correctly`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()

        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)
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
        val noFace = null

        every { faceDetector.analyze(frame) } returnsMany listOf(
            smallFace,
            bigFace,
            yawedFace,
            rolledFace,
            noFace,
        )

        val detections = viewModel.currentDetection.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        viewModel.startCapture()

        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        detections.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.TOOFAR)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.TOOCLOSE)
            assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.OFFYAW)
            assertThat(it[3]?.status).isEqualTo(FaceDetection.Status.OFFROLL)
            assertThat(it[4]?.status).isEqualTo(FaceDetection.Status.NOFACE)
        }

        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any()) }
    }

    @Test
    fun `Process invalid faces after single fallback correctly`() = runTest {
        val validFace: Face = getFace()
        val badQuality: Face = getFace(quality = -2f)

        every { faceDetector.analyze(frame) } returnsMany listOf(
            badQuality, // not a fallback image due to bad quality
            validFace, // fallback image
            validFace, // 1st capture
            badQuality, // 2nd capture
        )

        val detections = viewModel.currentDetection.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        // fallback image frames before the preparation delay elapses
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        detections.observedValues.let {
            // fallback image frame wasn't observed during preparation delay
            assertThat(it[0]?.hasValidStatus()).isEqualTo(true)
            assertThat(it[1]?.hasValidStatus()).isEqualTo(true)
        }
    }

    @Test
    fun `Use default imaging duration when not configured`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()
        coEvery {
            configRepository.getProjectConfiguration().custom
        } returns mapOf("faceAutoCaptureImagingDurationMillis" to JsonPrimitive(AUTO_CAPTURE_IMAGING_DURATION_MS))

        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)

        advanceTimeBy(1)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
    }

    @Test
    fun `Use custom imaging duration when provided in config`() = runTest {
        val configDuration = 5000L
        coEvery { faceDetector.analyze(frame) } returns getFace()
        coEvery { configRepository.getProjectConfiguration().custom } returns
            mapOf(
                "faceAutoCaptureImagingDurationMillis" to JsonPrimitive(configDuration.toInt()),
            )

        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceTimeBy(configDuration / 2)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
        // Add 1ms to account for json deserialization delay
        advanceTimeBy((configDuration / 2) + 1)
        assertThat(capturingState.observedValues.last())
            .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
    }

    @Test
    fun `Auto capture spoof check Recorded finishes regardless of score`() = runTest {
        val validFace = getFace()
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns
            FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.RECORDED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.9f)

        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingState.observedValues).contains(LiveFeedbackFragmentViewModel.CapturingState.VALIDATING)
        assertThat(capturingState.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        assertThat(viewModel.sortedQualifyingCaptures).hasSize(1)
        assertThat(viewModel.sortedQualifyingCaptures[0].spoofCheckResult?.score).isEqualTo(0.9f)
    }

    @Test
    fun `Auto capture spoof check Enforced passed finishes capture`() = runTest {
        val validFace = getFace()
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns
            FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.1f)

        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingState.observedValues).contains(LiveFeedbackFragmentViewModel.CapturingState.VALIDATING)
        assertThat(capturingState.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        assertThat(viewModel.sortedQualifyingCaptures).hasSize(1)
        assertThat(viewModel.sortedQualifyingCaptures[0].spoofCheckResult?.score).isEqualTo(0.1f)
    }

    @Test
    fun `Auto capture spoof check Enforced failed resets state`() = runTest {
        val validFace = getFace()
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns
            FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.9f)

        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingState.observedValues).contains(LiveFeedbackFragmentViewModel.CapturingState.VALIDATION_FAILED)
        assertThat(capturingState.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
        assertThat(viewModel.userCaptures).isEmpty()
        assertThat(viewModel.sortedQualifyingCaptures).isEmpty()
    }

    @Test
    fun `Auto capture spoof check Enforced failed max attempts finishes capture`() = runTest {
        val validFace = getFace()
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns
            FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.9f)

        val capturingState = viewModel.capturingState.testObserver()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)

        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(capturingState.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)

        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(capturingState.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
    }

    @Test
    fun `Skip frame processing while validating`() = runTest {
        val validFace = getFace()
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns
            FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } answers {
            viewModel.process(frame, frame)
            SpoofCheckResult(score = 0.9f)
        }

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        verify(exactly = 1) { faceDetector.analyze(frame) }
    }

    @Test
    fun `Save all valid captures without fallback image`() = runTest {
        val validFace: Face = getFace()
        every { faceDetector.analyze(frame) } returns validFace

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()
        val samplesToKeep = 2
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, samplesToKeep, 0)
        viewModel.process(frame, frame) // won't be observed during the preparation phase
        viewModel.startCapture()
        repeat(100) {
            viewModel.process(frame, frame)
        }
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        currentDetectionObserver.observedValues.let {
            // 1st frame wasn't observed during preparation delay
            assertThat(it[0]?.hasValidStatus()).isEqualTo(true)
            assertThat(it[1]?.hasValidStatus()).isEqualTo(true)
        }

        capturingStateObserver.observedValues.let {
            assertThat(it[0])
                .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
            assertThat(it[1])
                .isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
            assertThat(it[2]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
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
