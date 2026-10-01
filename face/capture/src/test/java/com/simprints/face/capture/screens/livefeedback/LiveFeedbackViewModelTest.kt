package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.models.FaceTrackingConfiguration
import com.simprints.face.capture.screens.CaptureAttemptTracker
import com.simprints.face.capture.usecases.CropToFaceSquareUseCase
import com.simprints.face.capture.usecases.GetFaceTrackingConfigurationUseCase
import com.simprints.face.capture.usecases.GetSpoofCheckConfigurationUseCase
import com.simprints.face.capture.usecases.IsUsingAutoCaptureUseCase
import com.simprints.face.capture.usecases.SelectDominantFaceUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.FaceSelector
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
import kotlinx.coroutines.flow.filter
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
import kotlin.time.Duration.Companion.milliseconds

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

    @MockK
    private lateinit var getFaceTrackingConfiguration: GetFaceTrackingConfigurationUseCase

    private lateinit var viewModel: LiveFeedbackViewModel

    private val testCaptureAttemptTracker = CaptureAttemptTracker()

    /**
     * The square geometry is exercised for real so the status rules are genuinely tested; only the
     * bitmap cropping itself is stubbed out, since [frame] is a mock.
     */
    private val cropToFaceSquare = spyk(CropToFaceSquareUseCase())

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { frame.width } returns FRAME_SIZE_PX
        every { frame.height } returns FRAME_SIZE_PX
        every { cropToFaceSquare.invoke(any(), any(), any()) } returns frame
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
        // The flag is off by default, so the bulk of the suite covers the standard cutout capture
        every { getFaceTrackingConfiguration.invoke(any()) } returns FaceTrackingConfiguration.DISABLED
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns SpoofCheckConfiguration.DISABLED

        every { timeHelper.now() } returnsMany (0..1000L).map { Timestamp(it) }
        justRun { frame.recycle() }

        viewModel = LiveFeedbackViewModel(
            resolveFaceBioSdkUseCase,
            configRepository,
            eventReporter,
            timeHelper,
            isUsingAutoCapture,
            getFaceTrackingConfiguration,
            getSpoofCheckConfiguration,
            testCaptureAttemptTracker,
            cropToFaceSquare,
            SelectDominantFaceUseCase(),
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
        enableAutoCapture()

        viewModel.initAutoCapture()
        advanceUntilIdle()

        assertThat(viewModel.state.value.isAutoCapture).isTrue()
        assertThat(viewModel.isAutoCapture).isTrue()
    }

    @Test
    fun `onScreenResumed with denied permission auto-requests only once`() = runTest {
        val actions = mutableListOf<LiveFeedbackViewModel.PermissionAction>()
        backgroundScope.launch(testCoroutineRule.testCoroutineDispatcher) {
            viewModel.permissionActions.toList(actions)
        }

        viewModel.onScreenResumed(PermissionStatus.Denied)
        viewModel.onScreenResumed(PermissionStatus.Denied)
        advanceUntilIdle()

        assertThat(actions).containsExactly(LiveFeedbackViewModel.PermissionAction.RequestCameraPermission)
        assertThat(viewModel.state.value.permissionStatus).isEqualTo(PermissionStatus.Denied)
    }

    @Test
    fun `permission button opens settings when permission is denied forever`() = runTest {
        val actions = mutableListOf<LiveFeedbackViewModel.PermissionAction>()
        backgroundScope.launch(testCoroutineRule.testCoroutineDispatcher) {
            viewModel.permissionActions.toList(actions)
        }

        viewModel.onPermissionResult(PermissionStatus.DeniedNeverAskAgain)
        viewModel.onPermissionButtonClicked()
        advanceUntilIdle()

        assertThat(actions).containsExactly(LiveFeedbackViewModel.PermissionAction.OpenAppSettings)
    }

    @Test
    fun `manual - valid face before start keeps NOT_STARTED, shows VALID feedback and stores fallback`() = runTest {
        detects(getFace())

        screenReady()
        processFrame()

        with(viewModel.state.value) {
            assertThat(phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
            assertThat(feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
            assertThat(progress.visible).isFalse()
        }
        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `manual - starting capture moves to CAPTURING and valid frames become VALID_CAPTURING`() = runTest {
        detects(getFace())

        capturingAfterFallback(2)
        processFrame()

        with(viewModel.state.value) {
            assertThat(phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)
            assertThat(feedback).isEqualTo(LiveFeedbackState.Feedback.VALID_CAPTURING)
            assertThat(progress.visible).isTrue()
            assertThat(progress.tint).isEqualTo(Progress.Tint.VALID)
        }
    }

    @Test
    fun `manual - invalid faces map to the correct feedback`() = runTest {
        detectsInTurn(
            getFace(Rect(0, 0, 10, 10)), // too far - 100px on a 1000px frame
            getFace(Rect(0, 0, 110, 110)), // too close - the square cannot fit the frame
            getFace(yaw = 45f), // off yaw
            getFace(roll = 45f), // off roll
            getFace(quality = 0f),
            null, // no face
        )
        val states = collectStates()

        screenReady(2)
        processFrame(6)

        val feedbacks = states.map { it.feedback }
        assertThat(feedbacks).containsExactly(
            LiveFeedbackState.Feedback.NONE,
            LiveFeedbackState.Feedback.TOO_FAR,
            LiveFeedbackState.Feedback.TOO_CLOSE,
            LiveFeedbackState.Feedback.LOOK_STRAIGHT,
            LiveFeedbackState.Feedback.BAD_QUALITY,
            LiveFeedbackState.Feedback.NO_FACE,
        )
        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `manual - bad quality faces considered valid after a valid fallback capture`() = runTest {
        detectsInTurn(
            getFace(quality = 0f),
            getFace(),
            getFace(yaw = 45f), // to switch out the result
            getFace(quality = 0f),
        )
        val states = collectStates()

        screenReady(2)
        processFrame(5)

        val feedbacks = states.map { it.feedback }
        assertThat(feedbacks).containsExactly(
            LiveFeedbackState.Feedback.NONE,
            LiveFeedbackState.Feedback.BAD_QUALITY,
            LiveFeedbackState.Feedback.VALID,
            LiveFeedbackState.Feedback.LOOK_STRAIGHT,
            LiveFeedbackState.Feedback.VALID,
        )
    }

    @Test
    fun `manual - progress reflects captured sample ratio`() = runTest {
        detects(getFace())

        capturing(2)
        processFrame()

        assertThat(viewModel.state.value.progress.value).isEqualTo(0.5f)
    }

    @Test
    fun `manual - capturing enough samples finishes and publishes sorted result`() = runTest {
        val validFace = getFace()
        detects(validFace)
        val states = collectStates()

        capturingAfterFallback(2)
        processFrame()
        processFrame()

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
    fun `manual - frames arriving while finishing is still in progress are dropped, not appended`() = runTest {
        val validFace = getFace()
        detects(validFace)
        // Simulate the camera delivering another frame while finishCapture()
        every { faceDetector.analyze(frame, true, any()) } answers {
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)
            processFrame()
            validFace
        }

        capturing()
        processFrame() // reaches the requested sample count and triggers finishCapture()

        assertThat(viewModel.userCaptures).hasSize(1)
        verify(exactly = 1) { faceDetector.analyze(frame, false, any()) }
    }

    @Test
    fun `auto - does not start until start capture is pressed`() = runTest {
        enableAutoCapture()
        detects(getFace())

        screenReady()
        processFrame()

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        // Guidance is suppressed before imaging starts in auto-capture.
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.NONE)
    }

    @Test
    fun `auto - held off capture does not start`() = runTest {
        enableAutoCapture()
        detects(getFace())

        capturing()
        viewModel.holdOffAutoCapture()
        processFrame()

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
    }

    @Test
    fun `auto - valid face after start begins CAPTURING and finishes after imaging duration`() = runTest {
        enableAutoCapture()
        detects(getFace())
        val states = collectStates()

        capturing()
        processFrame()

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
    fun `auto - frames arriving while finishing is still in progress are dropped, not appended`() = runTest {
        enableAutoCapture()
        val validFace = getFace()
        detects(validFace)

        var elapsedMs = 0L
        every { timeHelper.now() } answers { Timestamp(elapsedMs) }

        // Simulate a frame arriving while finishCapture() is still running
        every { faceDetector.analyze(frame, true, any()) } answers {
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)
            processFrame()
            validFace
        }

        capturing()
        processFrame() // begins CAPTURING at t=0

        elapsedMs = AUTO_CAPTURE_IMAGING_DURATION_MS + 1
        advanceTimeBy((AUTO_CAPTURE_IMAGING_DURATION_MS + 1).milliseconds) // fires the timeout job -> finishCapture()

        // Only the original sample was ever captured - the frame injected mid-finish was dropped.
        assertThat(viewModel.userCaptures).hasSize(1)
        verify(exactly = 1) { faceDetector.analyze(frame, false, any()) }
    }

    @Test
    fun `auto - invalid faces map to the correct feedback`() = runTest {
        enableAutoCapture()
        detectsInTurn(
            getFace(Rect(0, 0, 10, 10)), // too far - 100px on a 1000px frame
            getFace(Rect(0, 0, 110, 110)), // too close - the square cannot fit the frame
            getFace(yaw = 45f), // off yaw
            getFace(roll = 45f), // off roll
            getFace(quality = 0f), // bad quality
            null, // no face
        )
        val states = collectStates()

        capturing()
        processFrame(6)
        advanceUntilIdle()

        val feedbacks = states.map { it.feedback }
        assertThat(feedbacks).containsAtLeast(
            LiveFeedbackState.Feedback.NONE, // This may repeat multiple time so cannot do exact comparison
            LiveFeedbackState.Feedback.TOO_FAR,
            LiveFeedbackState.Feedback.TOO_CLOSE,
            LiveFeedbackState.Feedback.LOOK_STRAIGHT,
            LiveFeedbackState.Feedback.BAD_QUALITY,
            LiveFeedbackState.Feedback.NO_FACE,
        )
        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `auto - returns correct amount of valid faces after finishing`() = runTest {
        enableAutoCapture()
        detectsInTurn(
            getFace(Rect(0, 0, 10, 10)), // too far - 100px on a 1000px frame
            getFace(quality = 0.95f), // good
            getFace(quality = 0f), // bad quality
            getFace(quality = 0.9f), // good, but will be replaced
            getFace(quality = 0.97f), // good
            null, // no face
        )
        val states = collectStates()

        capturing(2)
        processFrame(7)
        advanceUntilIdle()

        assertThat(viewModel.sortedQualifyingCaptures).hasSize(2)
        assertThat(viewModel.sortedQualifyingCaptures.map { it.face?.quality }).containsExactly(0.97f, 0.95f)
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `spoof RECORDED finishes regardless of score`() = runTest {
        enableSpoofCheck()
        detects(getFace())
        val states = collectStates()

        capturingAfterFallback()
        processFrame()
        advanceUntilIdle()

        assertThat(states.map { it.phase }).contains(LiveFeedbackState.Phase.VALIDATING)
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
        assertThat(viewModel.sortedQualifyingCaptures[0].spoofCheckResult?.score).isEqualTo(0.9f)
    }

    @Test
    fun `spoof ENFORCED passing finishes capture`() = runTest {
        enableSpoofCheck(FaceConfiguration.SpoofCheckMode.ENFORCED, score = 0.1f)
        detects(getFace())

        capturingAfterFallback()
        processFrame()
        advanceUntilIdle()

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
    }

    @Test
    fun `spoof ENFORCED failing goes through VALIDATION_FAILED and resets to NOT_STARTED`() = runTest {
        enableSpoofCheck(FaceConfiguration.SpoofCheckMode.ENFORCED)
        detects(getFace())
        val states = collectStates()

        capturingAfterFallback()
        processFrame()
        advanceUntilIdle()

        assertThat(states.map { it.phase }).contains(LiveFeedbackState.Phase.VALIDATION_FAILED)
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        assertThat(viewModel.userCaptures).isEmpty()
        assertThat(viewModel.sortedQualifyingCaptures).isEmpty()
    }

    @Test
    fun `spoof ENFORCED failing max attempts finishes capture`() = runTest {
        enableSpoofCheck(FaceConfiguration.SpoofCheckMode.ENFORCED)
        detects(getFace())

        screenReady()

        // Attempt 1
        processFrame()
        viewModel.startCapture()
        processFrame()
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)

        // Attempt 2 reaches maxAttempts
        processFrame()
        viewModel.startCapture()
        processFrame()
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
    }

    @Test
    fun `spoof ENFORCED failing retry reports an incrementing attempt number`() = runTest {
        enableSpoofCheck(FaceConfiguration.SpoofCheckMode.ENFORCED)
        detects(getFace())
        val attemptNumbers = mutableListOf<Int>()
        coEvery {
            eventReporter.addCaptureEvents(any(), capture(attemptNumbers), any(), any(), any())
        } just runs

        screenReady()

        // Attempt 0 fails spoof check
        processFrame()
        viewModel.startCapture()
        processFrame()
        advanceUntilIdle()

        // Attempt 1 (retry) reaches maxAttempts and finishes
        processFrame()
        viewModel.startCapture()
        processFrame()
        advanceUntilIdle()

        assertThat(attemptNumbers).containsAtLeast(0, 1)
    }

    @Test
    fun `frames are skipped while validating and progress uses the validation tint`() = runTest {
        enableSpoofCheck()
        detects(getFace())
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } answers {
            // A frame arriving mid-validation must not trigger another analysis.
            processFrame()
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.VALIDATING)
            assertThat(viewModel.state.value.progress.tint).isEqualTo(Progress.Tint.VALIDATION)
            SpoofCheckResult(score = 0.1f)
        }

        capturing()
        processFrame()
        advanceUntilIdle()

        verify(exactly = 1) { faceDetector.analyze(frame, false, any()) }
    }

    @Test
    fun `event saving - fallback capture event is saved only once across multiple valid pre-start frames`() = runTest {
        detects(getFace())

        screenReady()
        processFrame()
        processFrame()
        processFrame()

        // A single fallback event despite several valid frames, and no capture events yet.
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `event saving - single sample capture saves one capture event and the fallback`() = runTest {
        detects(getFace())

        capturingAfterFallback()
        processFrame() // captured sample

        // 1 captured sample + 1 fallback capture.
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `event saving - captured samples are stored as non-fallback with one event per sample plus fallback`() = runTest {
        val validFace = getFace()
        detects(validFace)
        every { faceDetector.analyze(any(), true, any()) } returns null

        capturingAfterFallback(2)
        processFrame()
        processFrame()

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
    fun `event saving - enriches only the final accepted captures with age and gender`() = runTest {
        val validFace = getFace()
        val enrichedFace = getFace().copy(age = 34f, gender = Face.Gender(0.2f, 0.8f))
        detects(validFace)
        every { faceDetector.analyze(any(), true, any()) } returns enrichedFace

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.RANK_ONE, 1)
        processFrame() // fallback frame before start
        viewModel.startCapture()
        processFrame() // captured sample -> finishes

        with(viewModel.sortedQualifyingCaptures) {
            assertThat(this).hasSize(1)
            assertThat(first().face?.age).isEqualTo(34f)
            assertThat(first().face?.gender).isEqualTo(Face.Gender(0.2f, 0.8f))
        }
        // Once for the captured sample and once for the fallback capture.
        verify(exactly = 2) { faceDetector.analyze(any(), true, any()) }
    }

    @Test
    fun `event saving - age and gender estimation runs during CAPTURING for every spoof-check retry`() = runTest {
        // The default score sits above the threshold, so every attempt fails
        enableSpoofCheck(FaceConfiguration.SpoofCheckMode.ENFORCED)
        detects(getFace())
        every { faceDetector.analyze(any(), true, any()) } returns getFace()

        screenReady()

        // Attempt 1 fails and gets discarded, but enrichment already ran while still CAPTURING,
        processFrame()
        viewModel.startCapture()
        processFrame()
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        // Once for the captured sample and once for the fallback capture.
        verify(exactly = 2) { faceDetector.analyze(any(), true, any()) }

        // Attempt 2 reaches maxAttempts and finishes despite still failing spoof check.
        processFrame()
        viewModel.startCapture()
        processFrame()
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)

        // Enrichment runs again for the second attempt's captures + fallback.
        verify(exactly = 4) { faceDetector.analyze(any(), true, any()) }
    }

    @Test
    fun `event saving - disabled spoof check never shows VALIDATING phase or the orange validation tint`() = runTest {
        // Default configuration from setUp() is SpoofCheckConfiguration.DISABLED.
        val validFace = getFace()
        detects(validFace)
        every { faceDetector.analyze(any(), true, any()) } returns validFace
        val states = collectStates()

        capturingAfterFallback()
        processFrame() // captured sample -> finishes
        advanceUntilIdle()

        assertThat(states.map { it.phase }).contains(LiveFeedbackState.Phase.FINISHED)
        // Age/gender enrichment must never trigger
        assertThat(states.map { it.phase }).doesNotContain(LiveFeedbackState.Phase.VALIDATING)
        assertThat(states.map { it.progress.tint }).doesNotContain(Progress.Tint.VALIDATION)
        coVerify(exactly = 0) { faceDetector.spoofCheck(any(), any(), any()) }
    }

    @Test
    fun `event saving - falls back to the fallback capture when no captured sample qualifies`() = runTest {
        detectsInTurn(
            getFace(), // valid fallback frame before start
            null, // invalid captured sample (no face)
        )

        capturingAfterFallback()
        processFrame() // invalid capture -> finishes

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
        enableAutoCapture()
        detects(getFace())

        capturingAfterFallback()
        processFrame() // begins imaging
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        // 1 stored sample + 1 fallback.
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `target box is a square on the detection, kept inside the frame`() = runTest {
        detectsInTurn(
            trackedFace(Rect(250, 300, 550, 500)), // 300x200px centred at (400, 400) in a 1000px frame
            trackedFace(Rect(-150, 300, 150, 600)), // 300x300px centred on the left edge
        )

        enableFaceTracking()
        screenReady()

        processFrame()
        val centred = requireNotNull(viewModel.state.value.targetBox).rect
        // Side is the longer detection side (300px), normalised against the 1000px frame
        assertThat(centred.width()).isWithin(TOLERANCE).of(0.3f)
        assertThat(centred.height()).isWithin(TOLERANCE).of(0.3f)
        assertThat(centred.centerX()).isWithin(TOLERANCE).of(0.4f)
        assertThat(centred.centerY()).isWithin(TOLERANCE).of(0.4f)

        processFrame()
        val clamped = requireNotNull(viewModel.state.value.targetBox).rect
        assertThat(clamped.left).isAtLeast(0f)
        assertThat(clamped.top).isAtLeast(0f)
        assertThat(clamped.right).isAtMost(1f)
        assertThat(clamped.bottom).isAtMost(1f)
        assertThat(clamped.width()).isWithin(TOLERANCE).of(clamped.height())
    }

    @Test
    fun `target box is tinted by the detection status`() = runTest {
        detectsInTurn(
            trackedFace(), // valid
            trackedFace(yaw = 45f), // pose is off
            trackedFace(quality = 0f), // quality is off
            trackedFace(Rect(0, 0, 100, 100)), // below the minimum size
            trackedFace(Rect(0, 0, 950, 950)), // above the maximum size
        )
        val states = collectStates()

        enableFaceTracking()
        screenReady(5)
        processFrame(5)

        assertThat(states.mapNotNull { it.targetBox?.tint }).containsExactly(
            FaceTargetBox.Tint.VALID,
            FaceTargetBox.Tint.WARNING,
            FaceTargetBox.Tint.WARNING,
            FaceTargetBox.Tint.INVALID,
            FaceTargetBox.Tint.INVALID,
        )
    }

    @Test
    fun `target box is cleared when no face is detected`() = runTest {
        detectsInTurn(trackedFace(), null)

        enableFaceTracking()
        screenReady(2)
        processFrame()
        assertThat(viewModel.state.value.targetBox).isNotNull()

        processFrame()
        assertThat(viewModel.state.value.targetBox).isNull()
    }

    @Test
    fun `the square crop, not the full frame, is stored as the capture bitmap`() = runTest {
        val squareCrop = mockk<Bitmap>(relaxed = true)
        every { cropToFaceSquare.invoke(any(), any(), any()) } returns squareCrop
        detects(trackedFace())

        enableFaceTracking()
        capturing()
        processFrame()

        with(viewModel.userCaptures.first()) {
            assertThat(bitmap).isSameInstanceAs(squareCrop)
            assertThat(original).isSameInstanceAs(frame)
        }
    }

    @Test
    fun `cutout capture leaves the spoof check on the SDK's own face choice`() = runTest {
        enableSpoofCheck()
        detects(getFace())
        val selectorGiven = recordSpoofCheckSelector()

        capturing()
        processFrame()
        advanceUntilIdle()

        // Only one person can be in a cutout, so there is nothing for a policy to choose between
        // and the SDK is left to report its own best detection
        assertThat(selectorGiven.single()).isNull()
    }

    @Test
    fun `face tracking gives the spoof check a face to pick, since the frame may hold several`() = runTest {
        enableSpoofCheck()
        detects(trackedFace())
        val selectorGiven = recordSpoofCheckSelector()

        enableFaceTracking()
        capturing()
        processFrame()
        advanceUntilIdle()

        assertThat(selectorGiven.single()).isNotNull()
    }

    @Test
    fun `auto - a frame that does not qualify hands its own frames back`() = runTest {
        val analysed = trackedPreviewFrame()
        // Valid, so it reaches the qualifying check, but a better capture already fills the quota
        every { faceDetector.analyze(analysed, any(), any()) } returns trackedFace(quality = 0.5f)
        enableAutoCapture()

        enableFaceTracking()
        capturing()
        // First frame takes the only slot with a better face
        detects(trackedFace(quality = 0.9f))
        processFrame()
        viewModel.process(analysed, analysed)

        assertThat(
            viewModel.userCaptures
                .single()
                .face
                ?.quality,
        ).isEqualTo(0.9f)
        verify(atLeast = 1) { analysed.recycle() }
    }

    @Test
    fun `auto - a qualifying frame keeps its frames`() = runTest {
        val analysed = trackedPreviewFrame()
        every { faceDetector.analyze(analysed, any(), any()) } returns trackedFace()
        enableAutoCapture()
        every { cropToFaceSquare.invoke(analysed, any(), any()) } returns analysed

        enableFaceTracking()
        capturing()
        viewModel.process(analysed, analysed)

        assertThat(viewModel.userCaptures).hasSize(1)
        verify(exactly = 0) { analysed.recycle() }
    }

    @Test
    fun `a frame too poor to become the fallback hands its frames back`() = runTest {
        val analysed = trackedPreviewFrame()
        // Too far to be a fallback, so nothing keeps it
        every { faceDetector.analyze(analysed, any(), any()) } returns trackedFace(Rect(0, 0, 40, 40))

        enableFaceTracking()
        screenReady()
        viewModel.process(analysed, analysed)

        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_FAR)
        verify(atLeast = 1) { analysed.recycle() }
    }

    @Test
    fun `a superseded fallback hands its frames back`() = runTest {
        val first = trackedPreviewFrame()
        every { faceDetector.analyze(first, any(), any()) } returns trackedFace(quality = 0.5f)
        every { cropToFaceSquare.invoke(first, any(), any()) } returns first

        enableFaceTracking()
        screenReady()
        viewModel.process(first, first)
        verify(exactly = 0) { first.recycle() }

        // A better face takes over as the fallback, so the one it displaces is no longer needed
        val better = trackedPreviewFrame()
        every { faceDetector.analyze(better, any(), any()) } returns trackedFace(quality = 0.9f)
        every { cropToFaceSquare.invoke(better, any(), any()) } returns better
        viewModel.process(better, better)

        verify(atLeast = 1) { first.recycle() }
        verify(exactly = 0) { better.recycle() }
    }

    @Test
    fun `frames that are only looked at are never cropped`() = runTest {
        // Too far to be kept, so its only job is to produce feedback
        detects(trackedFace(Rect(0, 0, 40, 40)))

        enableFaceTracking()
        screenReady()
        processFrame()

        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_FAR)
        // The square is still measured, for the overlay - only the pixel work is skipped
        verify(exactly = 0) { cropToFaceSquare.invoke(any(), any(), any()) }
    }

    @Test
    fun `the frame the crop was taken from is released once the capture is kept`() = runTest {
        val analysed = mockk<Bitmap>(relaxed = true) {
            every { width } returns FRAME_SIZE_PX
            every { height } returns FRAME_SIZE_PX
        }
        val squareCrop = mockk<Bitmap>(relaxed = true)
        every { cropToFaceSquare.invoke(analysed, any(), any()) } returns squareCrop
        every { faceDetector.analyze(analysed, any(), any()) } returns trackedFace()

        enableFaceTracking()
        capturing()
        viewModel.process(frame, analysed)

        verify(exactly = 1) { analysed.recycle() }
        verify(exactly = 0) { squareCrop.recycle() }
        assertThat(viewModel.userCaptures.single().bitmap).isSameInstanceAs(squareCrop)
    }

    @Test
    fun `the stored frame survives when it is also the frame that was analysed`() = runTest {
        // Nothing cropped the frame on the way in, so the capture holds one bitmap under both names
        val squareCrop = mockk<Bitmap>(relaxed = true)
        every { cropToFaceSquare.invoke(frame, any(), any()) } returns squareCrop
        detects(trackedFace())

        enableFaceTracking()
        capturing()
        processFrame()

        with(viewModel.userCaptures.single()) {
            assertThat(bitmap).isSameInstanceAs(squareCrop)
            assertThat(original).isSameInstanceAs(frame)
        }
        // An enabled spoof check reads original back off the capture, so it has to still be usable
        verify(exactly = 0) { frame.recycle() }
    }

    @Test
    fun `the analysed frame survives when the square turns out to be unusable`() = runTest {
        // The use case hands the frame straight back rather than cropping
        every { cropToFaceSquare.invoke(any(), any(), any()) } returns frame
        detects(trackedFace())

        enableFaceTracking()
        capturing()
        processFrame()

        verify(exactly = 0) { frame.recycle() }
        assertThat(viewModel.userCaptures.single().bitmap).isSameInstanceAs(frame)
    }

    /**
     * Drives the detector the way the real SDKs do: it offers [faces] to the selector the view
     * model supplies and only "extracts" for the index that comes back, so these tests exercise
     * the selection wiring rather than assuming it.
     */
    @Test
    fun `the capture mode travels with the state, settled before the screen acts on it`() = runTest {
        // The UI renders one mode or the other off this flag, so it has to travel with the state
        assertThat(viewModel.state.value.isFaceTrackingEnabled).isFalse()

        enableFaceTracking()
        val states = collectStates()

        viewModel.initAutoCapture()

        assertThat(viewModel.state.value.isFaceTrackingEnabled).isTrue()
        // The screen picks its mode before any frame is processed, so it never renders one mode
        // and then switches to the other
        assertThat(states.first().isFaceTrackingEnabled).isTrue()
        assertThat(states.map { it.isFaceTrackingEnabled }.distinct()).containsExactly(true)
    }

    @Test
    fun `cutout - a tall face is judged by the area it fills, not by its longest side`() = runTest {
        detects(tallFace())

        screenReady()
        processFrame()

        // 45% by 95% is 43% of the area, which the cutout accepts
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
    }

    @Test
    fun `tracking - the same tall face is rejected on its longest side`() = runTest {
        detects(tallFace())

        enableFaceTracking()
        screenReady()
        processFrame()

        // The square is built from the longest side, and 95% of the frame is past the limit
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_CLOSE)
    }

    @Test
    fun `tracking - a face filling most of the preview is still accepted`() = runTest {
        // 88% of the frame: close enough that the cutout would have called it too close
        detects(trackedFace(Rect(60, 60, 940, 940)))

        enableFaceTracking()
        screenReady()
        processFrame()

        // Tracking follows the face across the preview, so it only objects once the square
        // stops fitting rather than as soon as the face grows past the cutout's target
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
    }

    @Test
    fun `cutout - no target box is reported, since the cutout draws itself`() = runTest {
        detects(getFace())

        screenReady()
        processFrame()

        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
        assertThat(viewModel.state.value.targetBox).isNull()
    }

    @Test
    fun `cutout - the cutout crop is kept as the capture bitmap and no square is taken`() = runTest {
        val analysed = mockk<Bitmap>(relaxed = true)
        every { faceDetector.analyze(analysed, any(), any()) } returns getFace()

        capturing()
        viewModel.process(frame, analysed)

        assertThat(viewModel.userCaptures.single().bitmap).isSameInstanceAs(analysed)
        assertThat(viewModel.userCaptures.single().original).isSameInstanceAs(frame)
        verify(exactly = 0) { cropToFaceSquare.squareFor(any(), any(), any()) }
    }

    @Test
    fun `tracking - the pixel floor decides, whatever share of the preview the face fills`() = runTest {
        val smallFrame = previewFrame(300)
        // Both faces fill ~47% of a 300px preview, which the proportional rule accepts either way,
        // so only their pixel count can tell them apart
        every { faceDetector.analyze(smallFrame, any(), any()) } returnsMany listOf(
            Face(300, 300, Rect(80, 80, 220, 220), 0f, 0f, 1f, Random.nextBytes(20), "format"), // 140px
            Face(300, 300, Rect(70, 70, 230, 230), 0f, 0f, 1f, Random.nextBytes(20), "format"), // 160px
        )

        enableFaceTracking()
        screenReady()

        // Too few pixels of face for any SDK to extract a template from, whatever it looks like
        viewModel.process(smallFrame, smallFrame)
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_FAR)

        // 160px clears the floor, so only the proportional rules have a say
        viewModel.process(smallFrame, smallFrame)
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
    }

    /** Records the selector handed to each spoof check, so the caller can assert on it. */
    private fun recordSpoofCheckSelector(): List<FaceSelector?> {
        val seen = mutableListOf<FaceSelector?>()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } answers {
            seen += thirdArg<FaceSelector?>()
            SpoofCheckResult(score = 0.9f)
        }
        return seen
    }

    @Test
    fun `tracking - the configured minimum face size decides what is too far`() = runTest {
        // 160px of face, which the built-in 150px floor would have accepted
        val smallFrame = previewFrame(300)
        every { faceDetector.analyze(smallFrame, any(), any()) } returns
            Face(300, 300, Rect(70, 70, 230, 230), 0f, 0f, 1f, Random.nextBytes(20), "format")

        enableFaceTracking(minFaceSizePx = 200)
        screenReady()
        viewModel.process(smallFrame, smallFrame)

        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_FAR)
    }

    @Test
    fun `tracking - the configured image cap is the one the crop is taken with`() = runTest {
        detects(trackedFace())

        enableFaceTracking(maxImageSizePx = 512)
        capturing()
        processFrame()

        verify { cropToFaceSquare.invoke(any(), any(), 512) }
    }

    @Test
    fun `tracking - progress follows the tracked face unless the placement flag is set`() = runTest {
        enableFaceTracking()
        viewModel.initAutoCapture()

        // The screen picks where to draw progress off the state, so the default has to travel too
        assertThat(viewModel.state.value.isProgressAroundCaptureButton).isFalse()
    }

    @Test
    fun `tracking - the progress placement is settled before the screen acts on it`() = runTest {
        enableFaceTracking(progressAroundCaptureButton = true)
        val states = collectStates()

        viewModel.initAutoCapture()

        assertThat(viewModel.state.value.isProgressAroundCaptureButton).isTrue()
        // Otherwise progress would be drawn on the face first and jump to the button
        assertThat(states.map { it.isProgressAroundCaptureButton }.distinct()).containsExactly(true)
    }

    /** A mocked frame of [size] square pixels, for the rules that count pixels rather than ratios. */

    /** Brings the screen up the way the fragment does, leaving it ready for frames. */
    private suspend fun screenReady(samples: Int = 1) {
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, samples)
    }

    /** [screenReady] followed by the capture button press. */
    private suspend fun capturing(samples: Int = 1) {
        screenReady(samples)
        viewModel.startCapture()
    }

    /**
     * [screenReady] plus one frame before the button, which the screen keeps as the fallback
     * capture, then the button itself.
     */
    private suspend fun capturingAfterFallback(samples: Int = 1) {
        screenReady(samples)
        processFrame()
        viewModel.startCapture()
    }

    /** Feeds the shared [frame] through [times], the way the analyzer would. */
    private fun processFrame(times: Int = 1) = repeat(times) { viewModel.process(frame, frame) }

    /** Switches the screen to auto-capture. Call before [screenReady] or [capturing]. */
    private fun enableAutoCapture() {
        every { isUsingAutoCapture.invoke(any()) } returns true
    }

    /** What the detector reports for [frame], the bitmap almost every test feeds in. */
    private fun detects(face: Face?) {
        every { faceDetector.analyze(frame, any(), any()) } returns face
    }

    /** One detection per processed frame, in order. */
    private fun detectsInTurn(vararg faces: Face?) {
        every { faceDetector.analyze(frame, any(), any()) } returnsMany faces.toList()
    }

    /** Turns the spoof check on, scoring every capture [score] unless a test overrides it. */
    private fun enableSpoofCheck(
        mode: FaceConfiguration.SpoofCheckMode = FaceConfiguration.SpoofCheckMode.RECORDED,
        score: Float = 0.9f,
    ) {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(mode)
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = score)
    }

    /** A frame mock the size of the tracked preview, distinct per call so recycling can be traced. */
    private fun trackedPreviewFrame() = previewFrame(FRAME_SIZE_PX)

    private fun previewFrame(size: Int) = mockk<Bitmap>(relaxed = true) {
        every { width } returns size
        every { height } returns size
    }

    /** Turns on the experimental whole-preview tracking behaviour for a single test. */
    private fun enableFaceTracking(
        minFaceSizePx: Int = FaceTrackingConfiguration.DISABLED.minFaceSizePx,
        maxImageSizePx: Int = FaceTrackingConfiguration.DISABLED.maxImageSizePx,
        progressAroundCaptureButton: Boolean = false,
    ) {
        every { getFaceTrackingConfiguration.invoke(any()) } returns FaceTrackingConfiguration(
            enabled = true,
            minFaceSizePx = minFaceSizePx,
            maxImageSizePx = maxImageSizePx,
            progressAroundCaptureButton = progressAroundCaptureButton,
        )
    }

    private fun detectorSees(vararg faces: Rect) {
        every { faceDetector.analyze(frame, false, any()) } answers {
            val selectFace = thirdArg<((List<Rect>) -> Int?)?>()
            val index = selectFace?.invoke(faces.toList()) ?: 0
            frameFace(faces[index])
        }
    }

    @Test
    fun `tracking - several faces in frame do not stop the capture`() = runTest {
        enableFaceTracking()
        detectorSees(Rect(100, 350, 400, 650), Rect(600, 350, 900, 650))

        capturing()
        processFrame()

        // One of them is chosen and the capture carries on rather than blocking on the ambiguity
        assertThat(viewModel.userCaptures).hasSize(1)
        assertThat(viewModel.state.value.targetBox).isNotNull()
    }

    /** A face whose bounding box is expressed in the mocked frame's own pixels. */
    private fun frameFace(rect: Rect) = Face(FRAME_SIZE_PX, FRAME_SIZE_PX, rect, 0f, 0f, 1f, Random.nextBytes(20), "format")

    @Test
    fun `the dominant face is the one handed to template extraction`() = runTest {
        val bystander = Rect(50, 50, 150, 150) // small, off in a corner
        val subject = Rect(250, 250, 750, 750) // large, centred and inside the valid size band
        detectorSees(bystander, subject)

        enableFaceTracking()
        capturing()
        processFrame()

        // The stored capture describes the subject, not the bystander the SDK listed first
        val stored = viewModel.userCaptures.single().face!!
        assertThat(stored.relativeBoundingBox.width()).isWithin(TOLERANCE).of(0.5f)
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID_CAPTURING)
    }

    /**
     * Collects the states the screen acts on. The pre-initialisation state is dropped, matching
     * the fragment, so these assertions describe rendered behaviour rather than start-up order.
     */
    private fun TestScope.collectStates(): List<LiveFeedbackState> {
        val states = mutableListOf<LiveFeedbackState>()
        backgroundScope.launch(testCoroutineRule.testCoroutineDispatcher) {
            viewModel.state.filter { it.stateInitialised }.toList(states)
        }
        return states
    }

    /**
     * A face sized for the cutout rules, which judge the share of the fixed target it fills.
     * [rect] is in the source image's own pixels, so it is relative to the 100x100 source below.
     */
    private fun getFace(
        rect: Rect = Rect(0, 0, 60, 60),
        quality: Float = 1f,
        yaw: Float = 0f,
        roll: Float = 0f,
    ) = Face(100, 100, rect, yaw, roll, quality, Random.nextBytes(20), "format")

    /**
     * A face taller than it is wide: 45% by 95% of the frame. The two capture modes disagree about
     * it, because the cutout measures the area it fills while face tracking measures its longest
     * side, and only the latter is over its limit.
     */
    private fun tallFace() = trackedFace(Rect(275, 25, 725, 975))

    /**
     * A face sized for the face tracking rules, which judge the face against the whole preview.
     * Its source matches the mocked frame, so [rect] is read directly as frame pixels rather than
     * being rescaled. The default fills half the frame, comfortably inside the valid band.
     */
    private fun trackedFace(
        rect: Rect = Rect(0, 0, 500, 500),
        quality: Float = 1f,
        yaw: Float = 0f,
        roll: Float = 0f,
    ) = Face(FRAME_SIZE_PX, FRAME_SIZE_PX, rect, yaw, roll, quality, Random.nextBytes(20), "format")

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
        private const val QUALITY_THRESHOLD = 0.5f
        private const val FRAME_SIZE_PX = 1000
        private const val TOLERANCE = 0.002f
        private const val AUTO_CAPTURE_IMAGING_DURATION_MS = 3000L
    }
}
