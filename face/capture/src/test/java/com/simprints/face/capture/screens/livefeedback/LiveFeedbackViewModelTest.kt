package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.usecases.GetSpoofCheckConfigurationUseCase
import com.simprints.face.capture.usecases.IsUsingAutoCaptureUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration.SpoofCheckConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Behaviour-pinning tests for the refactored [LiveFeedbackViewModel], written from
 * scratch against its single-source-of-truth [LiveFeedbackViewModel.state] API.
 *
 * These validate the state machine, feedback mapping and progress derivation that the
 * refactored UI relies on. The pre-existing view-model tests are intentionally not reused.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class LiveFeedbackViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var faceDetector: FaceDetector

    @MockK
    lateinit var frame: Bitmap

    @MockK
    lateinit var resolveFaceBioSdkUseCase: ResolveFaceBioSdkUseCase

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

    private lateinit var viewModel: LiveFeedbackViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { resolveFaceBioSdkUseCase.invoke(any()) } returns mockk {
            every { detector } returns faceDetector
        }
        coEvery {
            configRepository
                .getProjectConfiguration()
                .face
                ?.getSdkConfiguration(any())
                ?.qualityThreshold
        } returns QUALITY_THRESHOLD
        every { isUsingAutoCapture.invoke(any()) } returns false
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns SpoofCheckConfiguration.DISABLED

        every { timeHelper.now() } returnsMany (0..1000L).map { Timestamp(it) }
        justRun { frame.recycle() }

        viewModel = LiveFeedbackViewModel(
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
    fun `initial state is NOT_STARTED with no feedback and hidden progress`() = runTest {
        with(viewModel.state.value) {
            assertThat(phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
            assertThat(feedback).isEqualTo(LiveFeedbackState.Feedback.NONE)
            assertThat(isAutoCapture).isFalse()
            assertThat(progress.visible).isFalse()
        }
    }

    @Test
    fun `initAutoCapture reflects auto-capture flag in state`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true

        viewModel.initAutoCapture()

        assertThat(viewModel.state.value.isAutoCapture).isTrue()
        assertThat(viewModel.isAutoCapture).isTrue()
    }

    @Test
    fun `manual - valid face before start keeps NOT_STARTED, shows VALID feedback and stores fallback`() = runTest {
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)

        with(viewModel.state.value) {
            assertThat(phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
            assertThat(feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
            assertThat(progress.visible).isFalse()
        }
        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `manual - starting capture moves to CAPTURING and valid frames become VALID_CAPTURING`() = runTest {
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        with(viewModel.state.value) {
            assertThat(phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)
            assertThat(feedback).isEqualTo(LiveFeedbackState.Feedback.VALID_CAPTURING)
            assertThat(progress.visible).isTrue()
            assertThat(progress.tint).isEqualTo(Progress.Tint.VALID)
        }
    }

    @Test
    fun `manual - invalid faces map to the correct feedback`() = runTest {
        every { faceDetector.analyze(frame) } returnsMany listOf(
            getFace(Rect(0, 0, 30, 30)), // too far
            getFace(Rect(0, 0, 80, 80)), // too close
            getFace(yaw = 45f), // off yaw
            getFace(roll = 45f), // off roll
            null, // no face
        )
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        repeat(5) { viewModel.process(frame, frame) }

        val feedbacks = states.map { it.feedback }
        assertThat(feedbacks).containsAtLeast(
            LiveFeedbackState.Feedback.TOO_FAR,
            LiveFeedbackState.Feedback.TOO_CLOSE,
            LiveFeedbackState.Feedback.LOOK_STRAIGHT,
            LiveFeedbackState.Feedback.NO_FACE,
        )
        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `manual - progress reflects captured sample ratio`() = runTest {
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.progress.value).isEqualTo(0.5f)
    }

    @Test
    fun `manual - capturing enough samples finishes and publishes sorted result`() = runTest {
        val validFace = getFace()
        every { faceDetector.analyze(frame) } returns validFace
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        val phases = states.map { it.phase }
        assertThat(phases)
            .containsAtLeast(
                LiveFeedbackState.Phase.NOT_STARTED,
                LiveFeedbackState.Phase.CAPTURING,
                LiveFeedbackState.Phase.FINISHED,
            ).inOrder()

        assertThat(viewModel.userCaptures).hasSize(2)
        assertThat(viewModel.sortedQualifyingCaptures).hasSize(2)
        assertThat(viewModel.state.value.result).hasSize(2)
        coVerify(exactly = 3) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `auto - does not start until start capture is pressed`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        // Guidance is suppressed before imaging starts in auto-capture.
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.NONE)
    }

    @Test
    fun `auto - held off capture does not start`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.holdOffAutoCapture()
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
    }

    @Test
    fun `auto - valid face after start begins CAPTURING and finishes after imaging duration`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame) } returns getFace()
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)

        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        assertThat(states.map { it.phase })
            .containsAtLeast(
                LiveFeedbackState.Phase.CAPTURING,
                LiveFeedbackState.Phase.FINISHED,
            ).inOrder()
        coVerify { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `spoof RECORDED finishes regardless of score`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig()
        every { faceDetector.analyze(frame) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any()) } returns SpoofCheckResult(score = 0.9f)
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(states.map { it.phase }).contains(LiveFeedbackState.Phase.VALIDATING)
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
        assertThat(viewModel.sortedQualifyingCaptures[0].spoofCheckResult?.score).isEqualTo(0.9f)
    }

    @Test
    fun `spoof ENFORCED passing finishes capture`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(FaceConfiguration.SpoofCheckMode.ENFORCED)
        every { faceDetector.analyze(frame) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any()) } returns SpoofCheckResult(score = 0.1f)

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
    }

    @Test
    fun `spoof ENFORCED failing goes through VALIDATION_FAILED and resets to NOT_STARTED`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(FaceConfiguration.SpoofCheckMode.ENFORCED)
        every { faceDetector.analyze(frame) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any()) } returns SpoofCheckResult(score = 0.9f)
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(states.map { it.phase }).contains(LiveFeedbackState.Phase.VALIDATION_FAILED)
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        assertThat(viewModel.userCaptures).isEmpty()
        assertThat(viewModel.sortedQualifyingCaptures).isEmpty()
    }

    @Test
    fun `spoof ENFORCED failing max attempts finishes capture`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(FaceConfiguration.SpoofCheckMode.ENFORCED)
        every { faceDetector.analyze(frame) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any()) } returns SpoofCheckResult(score = 0.9f)

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)

        // Attempt 1
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)

        // Attempt 2 reaches maxAttempts
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
    }

    @Test
    fun `frames are skipped while validating and progress uses the validation tint`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig()
        every { faceDetector.analyze(frame) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any()) } answers {
            // A frame arriving mid-validation must not trigger another analysis.
            viewModel.process(frame, frame)
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.VALIDATING)
            assertThat(viewModel.state.value.progress.tint).isEqualTo(Progress.Tint.VALIDATION)
            SpoofCheckResult(score = 0.1f)
        }

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        verify(exactly = 1) { faceDetector.analyze(frame) }
    }

    @Test
    fun `event saving - fallback capture event is saved only once across multiple valid pre-start frames`() = runTest {
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        // A single fallback event despite several valid frames, and no capture events yet.
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `event saving - single sample capture saves one capture event and the fallback`() = runTest {
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame) // fallback frame before start
        viewModel.startCapture()
        viewModel.process(frame, frame) // captured sample

        // 1 captured sample + 1 fallback capture.
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `event saving - captured samples are stored as non-fallback with one event per sample plus fallback`() = runTest {
        val validFace = getFace()
        every { faceDetector.analyze(frame) } returns validFace

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        viewModel.process(frame, frame) // fallback frame before start
        viewModel.startCapture()
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        assertThat(viewModel.userCaptures).hasSize(2)
        assertThat(viewModel.userCaptures.none { it.isFallback }).isTrue()
        with(viewModel.sortedQualifyingCaptures) {
            assertThat(this).hasSize(2)
            assertThat(all { it.face == validFace }).isTrue()
            assertThat(none { it.isFallback }).isTrue()
        }
        // 2 captures + 1 fallback.
        coVerify(exactly = 3) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `event saving - falls back to the fallback capture when no captured sample qualifies`() = runTest {
        every { faceDetector.analyze(frame) } returnsMany listOf(
            getFace(), // valid fallback frame before start
            null, // invalid captured sample (no face)
        )

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame) // fallback frame
        viewModel.startCapture()
        viewModel.process(frame, frame) // invalid capture -> finishes

        with(viewModel.sortedQualifyingCaptures) {
            assertThat(this).hasSize(1)
            assertThat(first().isFallback).isTrue()
        }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
        // Invalid capture + fallback are both tracked for analytics.
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `event saving - auto capture saves an event per stored sample plus the fallback`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame) // pre-start fallback frame (held off)
        viewModel.startCapture()
        viewModel.process(frame, frame) // begins imaging
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        // 1 stored sample + 1 fallback.
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    private fun TestScope.collectStates(): List<LiveFeedbackState> {
        val states = mutableListOf<LiveFeedbackState>()
        backgroundScope.launch(testCoroutineRule.testCoroutineDispatcher) {
            viewModel.state.toList(states)
        }
        return states
    }

    private fun getFace(
        rect: Rect = Rect(0, 0, 60, 60),
        quality: Float = 1f,
        yaw: Float = 0f,
        roll: Float = 0f,
    ) = Face(100, 100, rect, yaw, roll, quality, Random.nextBytes(20), "format")

    private fun spoofConfig(mode: FaceConfiguration.SpoofCheckMode = FaceConfiguration.SpoofCheckMode.RECORDED): SpoofCheckConfiguration =
        SpoofCheckConfiguration(
            mode = mode,
            threshold = 0.5f,
            maxAttempts = 2,
            maxBitmapSize = 1500,
            validationUiDurationMs = 1000,
            validationErrorUiDurationMs = 1000,
        )

    companion object {
        private const val QUALITY_THRESHOLD = -1f
        private const val AUTO_CAPTURE_IMAGING_DURATION_MS = 3000L
    }
}
