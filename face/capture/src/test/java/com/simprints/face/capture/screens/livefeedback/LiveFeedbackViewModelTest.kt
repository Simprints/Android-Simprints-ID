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
        every { isUsingAutoCapture.invoke(any()) } returns true

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
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
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
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
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
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(
            getFace(Rect(0, 0, 10, 10)), // too far - 100px on a 1000px frame
            getFace(Rect(0, 0, 110, 110)), // too close - the square cannot fit the frame
            getFace(yaw = 45f), // off yaw
            getFace(roll = 45f), // off roll
            getFace(quality = 0f),
            null, // no face
        )
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
        repeat(6) { viewModel.process(frame, frame) }

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
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(
            getFace(quality = 0f),
            getFace(),
            getFace(yaw = 45f), // to switch out the result
            getFace(quality = 0f),
        )
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
        repeat(5) { viewModel.process(frame, frame) }

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
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.progress.value).isEqualTo(0.5f)
    }

    @Test
    fun `manual - capturing enough samples finishes and publishes sorted result`() = runTest {
        val validFace = getFace()
        every { faceDetector.analyze(frame, any(), any()) } returns validFace
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
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
    fun `manual - frames arriving while finishing is still in progress are dropped, not appended`() = runTest {
        val validFace = getFace()
        every { faceDetector.analyze(frame, any(), any()) } returns validFace
        // Simulate the camera delivering another frame while finishCapture()
        every { faceDetector.analyze(frame, true, any()) } answers {
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)
            viewModel.process(frame, frame)
            validFace
        }

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame) // reaches the requested sample count and triggers finishCapture()

        assertThat(viewModel.userCaptures).hasSize(1)
        verify(exactly = 1) { faceDetector.analyze(frame, false, any()) }
    }

    @Test
    fun `auto - does not start until start capture is pressed`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        // Guidance is suppressed before imaging starts in auto-capture.
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.NONE)
    }

    @Test
    fun `auto - held off capture does not start`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.holdOffAutoCapture()
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
    }

    @Test
    fun `auto - valid face after start begins CAPTURING and finishes after imaging duration`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
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
    fun `auto - frames arriving while finishing is still in progress are dropped, not appended`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        val validFace = getFace()
        every { faceDetector.analyze(frame, any(), any()) } returns validFace

        var elapsedMs = 0L
        every { timeHelper.now() } answers { Timestamp(elapsedMs) }

        // Simulate a frame arriving while finishCapture() is still running
        every { faceDetector.analyze(frame, true, any()) } answers {
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.CAPTURING)
            viewModel.process(frame, frame)
            validFace
        }

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame) // begins CAPTURING at t=0

        elapsedMs = AUTO_CAPTURE_IMAGING_DURATION_MS + 1
        advanceTimeBy((AUTO_CAPTURE_IMAGING_DURATION_MS + 1).milliseconds) // fires the timeout job -> finishCapture()

        // Only the original sample was ever captured - the frame injected mid-finish was dropped.
        assertThat(viewModel.userCaptures).hasSize(1)
        verify(exactly = 1) { faceDetector.analyze(frame, false, any()) }
    }

    @Test
    fun `auto - invalid faces map to the correct feedback`() = runTest {
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(
            getFace(Rect(0, 0, 10, 10)), // too far - 100px on a 1000px frame
            getFace(Rect(0, 0, 110, 110)), // too close - the square cannot fit the frame
            getFace(yaw = 45f), // off yaw
            getFace(roll = 45f), // off roll
            getFace(quality = 0f), // bad quality
            null, // no face
        )
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        repeat(6) {
            viewModel.process(frame, frame)
        }
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
        every { isUsingAutoCapture.invoke(any()) } returns true
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(
            getFace(Rect(0, 0, 10, 10)), // too far - 100px on a 1000px frame
            getFace(quality = 0.95f), // good
            getFace(quality = 0f), // bad quality
            getFace(quality = 0.9f), // good, but will be replaced
            getFace(quality = 0.97f), // good
            null, // no face
        )
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
        viewModel.startCapture()
        repeat(7) {
            viewModel.process(frame, frame)
        }
        advanceUntilIdle()

        assertThat(viewModel.sortedQualifyingCaptures).hasSize(2)
        assertThat(viewModel.sortedQualifyingCaptures.map { it.face?.quality }).containsExactly(0.97f, 0.95f)
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `spoof RECORDED finishes regardless of score`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig()
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = 0.9f)
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
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
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = 0.1f)

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)
    }

    @Test
    fun `spoof ENFORCED failing goes through VALIDATION_FAILED and resets to NOT_STARTED`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(FaceConfiguration.SpoofCheckMode.ENFORCED)
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = 0.9f)
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
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
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = 0.9f)

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)

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
    fun `spoof ENFORCED failing retry reports an incrementing attempt number`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(FaceConfiguration.SpoofCheckMode.ENFORCED)
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = 0.9f)
        val attemptNumbers = mutableListOf<Int>()
        coEvery {
            eventReporter.addCaptureEvents(any(), capture(attemptNumbers), any(), any(), any())
        } just runs

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)

        // Attempt 0 fails spoof check
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        // Attempt 1 (retry) reaches maxAttempts and finishes
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(attemptNumbers).containsAtLeast(0, 1)
    }

    @Test
    fun `frames are skipped while validating and progress uses the validation tint`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig()
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } answers {
            // A frame arriving mid-validation must not trigger another analysis.
            viewModel.process(frame, frame)
            assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.VALIDATING)
            assertThat(viewModel.state.value.progress.tint).isEqualTo(Progress.Tint.VALIDATION)
            SpoofCheckResult(score = 0.1f)
        }

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        verify(exactly = 1) { faceDetector.analyze(frame, false, any()) }
    }

    @Test
    fun `event saving - fallback capture event is saved only once across multiple valid pre-start frames`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        // A single fallback event despite several valid frames, and no capture events yet.
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
        coVerify(exactly = 0) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `event saving - single sample capture saves one capture event and the fallback`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
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
        every { faceDetector.analyze(frame, any(), any()) } returns validFace
        every { faceDetector.analyze(any(), true, any()) } returns null

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
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
    fun `event saving - enriches only the final accepted captures with age and gender`() = runTest {
        val validFace = getFace()
        val enrichedFace = getFace().copy(age = 34f, gender = Face.Gender(0.2f, 0.8f))
        every { faceDetector.analyze(frame, any(), any()) } returns validFace
        every { faceDetector.analyze(any(), true, any()) } returns enrichedFace

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.RANK_ONE, 1)
        viewModel.process(frame, frame) // fallback frame before start
        viewModel.startCapture()
        viewModel.process(frame, frame) // captured sample -> finishes

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
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig(FaceConfiguration.SpoofCheckMode.ENFORCED)
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        every { faceDetector.analyze(any(), true, any()) } returns getFace()
        coEvery { faceDetector.spoofCheck(any(), any(), any()) } returns SpoofCheckResult(score = 0.9f) // always fails

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)

        // Attempt 1 fails and gets discarded, but enrichment already ran while still CAPTURING,
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.NOT_STARTED)
        // Once for the captured sample and once for the fallback capture.
        verify(exactly = 2) { faceDetector.analyze(any(), true, any()) }

        // Attempt 2 reaches maxAttempts and finishes despite still failing spoof check.
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()
        assertThat(viewModel.state.value.phase).isEqualTo(LiveFeedbackState.Phase.FINISHED)

        // Enrichment runs again for the second attempt's captures + fallback.
        verify(exactly = 4) { faceDetector.analyze(any(), true, any()) }
    }

    @Test
    fun `event saving - disabled spoof check never shows VALIDATING phase or the orange validation tint`() = runTest {
        // Default configuration from setUp() is SpoofCheckConfiguration.DISABLED.
        val validFace = getFace()
        every { faceDetector.analyze(frame, any(), any()) } returns validFace
        every { faceDetector.analyze(any(), true, any()) } returns validFace
        val states = collectStates()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame) // fallback frame before start
        viewModel.startCapture()
        viewModel.process(frame, frame) // captured sample -> finishes
        advanceUntilIdle()

        assertThat(states.map { it.phase }).contains(LiveFeedbackState.Phase.FINISHED)
        // Age/gender enrichment must never trigger
        assertThat(states.map { it.phase }).doesNotContain(LiveFeedbackState.Phase.VALIDATING)
        assertThat(states.map { it.progress.tint }).doesNotContain(Progress.Tint.VALIDATION)
        coVerify(exactly = 0) { faceDetector.spoofCheck(any(), any(), any()) }
    }

    @Test
    fun `event saving - falls back to the fallback capture when no captured sample qualifies`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(
            getFace(), // valid fallback frame before start
            null, // invalid captured sample (no face)
        )

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
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
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame) // pre-start fallback frame (held off)
        viewModel.startCapture()
        viewModel.process(frame, frame) // begins imaging
        advanceTimeBy(AUTO_CAPTURE_IMAGING_DURATION_MS + 1)

        // 1 stored sample + 1 fallback.
        coVerify(exactly = 2) { eventReporter.addCaptureEvents(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `target box tracks the detection as a square centred on it`() = runTest {
        // 300x200px detection centred at (400, 400) in a 1000x1000 frame
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace(Rect(250, 300, 550, 500))

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        val box = requireNotNull(viewModel.state.value.targetBox).rect
        // Side is the longer detection side (300px), normalised against the 1000px frame
        assertThat(box.width()).isWithin(TOLERANCE).of(0.3f)
        assertThat(box.height()).isWithin(TOLERANCE).of(0.3f)
        assertThat(box.centerX()).isWithin(TOLERANCE).of(0.4f)
        assertThat(box.centerY()).isWithin(TOLERANCE).of(0.4f)
    }

    @Test
    fun `target box stays inside the frame when the face is against the edge`() = runTest {
        // 300x300px detection whose centre sits on the left edge of the frame
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace(Rect(-150, 300, 150, 600))

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        val box = requireNotNull(viewModel.state.value.targetBox).rect
        assertThat(box.left).isAtLeast(0f)
        assertThat(box.top).isAtLeast(0f)
        assertThat(box.right).isAtMost(1f)
        assertThat(box.bottom).isAtMost(1f)
        assertThat(box.width()).isWithin(TOLERANCE).of(box.height())
    }

    @Test
    fun `target box is tinted by the detection status`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(
            trackedFace(), // valid
            trackedFace(yaw = 45f), // pose is off
            trackedFace(quality = 0f), // quality is off
            trackedFace(Rect(0, 0, 100, 100)), // below the minimum size
            trackedFace(Rect(0, 0, 950, 950)), // above the maximum size
        )
        val states = collectStates()

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 5)
        repeat(5) { viewModel.process(frame, frame) }

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
        every { faceDetector.analyze(frame, any(), any()) } returnsMany listOf(trackedFace(), null)

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2)
        viewModel.process(frame, frame)
        assertThat(viewModel.state.value.targetBox).isNotNull()

        viewModel.process(frame, frame)
        assertThat(viewModel.state.value.targetBox).isNull()
    }

    @Test
    fun `the square crop, not the full frame, is stored as the capture bitmap`() = runTest {
        val squareCrop = mockk<Bitmap>(relaxed = true)
        every { cropToFaceSquare.invoke(any(), any(), any()) } returns squareCrop
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace()

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        with(viewModel.userCaptures.first()) {
            assertThat(bitmap).isSameInstanceAs(squareCrop)
            assertThat(original).isSameInstanceAs(frame)
        }
    }

    @Test
    fun `cutout capture leaves the spoof check on the SDK's own face choice`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig()
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()
        val selectorGiven = recordSpoofCheckSelector()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        // Only one person can be in a cutout, so there is nothing for a policy to choose between
        // and the SDK is left to report its own best detection
        assertThat(selectorGiven.single()).isNull()
    }

    @Test
    fun `face tracking gives the spoof check a face to pick, since the frame may hold several`() = runTest {
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns spoofConfig()
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace()
        val selectorGiven = recordSpoofCheckSelector()

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        advanceUntilIdle()

        assertThat(selectorGiven.single()).isNotNull()
    }

    @Test
    fun `frames that are only looked at are never cropped`() = runTest {
        // Too far to be kept, so its only job is to produce feedback
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace(Rect(0, 0, 40, 40))

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

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
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, analysed)

        verify(exactly = 1) { analysed.recycle() }
        verify(exactly = 0) { squareCrop.recycle() }
        assertThat(viewModel.userCaptures.single().bitmap).isSameInstanceAs(squareCrop)
    }

    @Test
    fun `the analysed frame survives when the square turns out to be unusable`() = runTest {
        // The use case hands the frame straight back rather than cropping
        every { cropToFaceSquare.invoke(any(), any(), any()) } returns frame
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace()

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        verify(exactly = 0) { frame.recycle() }
        assertThat(viewModel.userCaptures.single().bitmap).isSameInstanceAs(frame)
    }

    /**
     * Drives the detector the way the real SDKs do: it offers [faces] to the selector the view
     * model supplies and only "extracts" for the index that comes back, so these tests exercise
     * the selection wiring rather than assuming it.
     */
    @Test
    fun `the capture mode is published in the state, not left for the UI to ask about`() = runTest {
        // The UI renders one mode or the other off this flag, so it has to travel with the state
        assertThat(viewModel.state.value.isFaceTrackingEnabled).isFalse()

        enableFaceTracking()
        viewModel.initAutoCapture()

        assertThat(viewModel.state.value.isFaceTrackingEnabled).isTrue()
    }

    @Test
    fun `the capture mode is known by the first state the screen acts on`() = runTest {
        enableFaceTracking()
        val states = collectStates()

        viewModel.initAutoCapture()

        // The screen picks its mode before any frame is processed, so it never renders one mode
        // and then switches to the other
        assertThat(states.first().isFaceTrackingEnabled).isTrue()
        assertThat(states.map { it.isFaceTrackingEnabled }.distinct()).containsExactly(true)
    }

    @Test
    fun `cutout - a tall face is judged by the area it fills, not by its longest side`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returns tallFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        // 45% by 95% is 43% of the area, which the cutout accepts
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
    }

    @Test
    fun `tracking - the same tall face is rejected on its longest side`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returns tallFace()

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        // The square is built from the longest side, and 95% of the frame is past the limit
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_CLOSE)
    }

    @Test
    fun `tracking - a face filling most of the preview is still accepted`() = runTest {
        // 88% of the frame: close enough that the cutout would have called it too close
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace(Rect(60, 60, 940, 940))

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        // Tracking follows the face across the preview, so it only objects once the square
        // stops fitting rather than as soon as the face grows past the cutout's target
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
    }

    @Test
    fun `cutout - no target box is reported, since the cutout draws itself`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(frame, frame)

        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.VALID)
        assertThat(viewModel.state.value.targetBox).isNull()
    }

    @Test
    fun `cutout - the cutout crop is kept as the capture bitmap and no square is taken`() = runTest {
        val analysed = mockk<Bitmap>(relaxed = true)
        every { faceDetector.analyze(analysed, any(), any()) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, analysed)

        assertThat(viewModel.userCaptures.single().bitmap).isSameInstanceAs(analysed)
        assertThat(viewModel.userCaptures.single().original).isSameInstanceAs(frame)
        verify(exactly = 0) { cropToFaceSquare.squareFor(any(), any(), any()) }
    }

    @Test
    fun `tracking - a face below the pixel floor is too far even when it fills the preview`() = runTest {
        // 140px of face on a 300px preview is 47% of it, which the proportional rule would accept
        val smallFrame = previewFrame(300)
        every { faceDetector.analyze(smallFrame, any(), any()) } returns
            Face(300, 300, Rect(80, 80, 220, 220), 0f, 0f, 1f, Random.nextBytes(20), "format")

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(smallFrame, smallFrame)

        // Too few pixels of face for any SDK to extract a template from, whatever it looks like
        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_FAR)
    }

    @Test
    fun `tracking - a face above the pixel floor on the same preview is accepted`() = runTest {
        val smallFrame = previewFrame(300)
        every { faceDetector.analyze(smallFrame, any(), any()) } returns
            Face(300, 300, Rect(70, 70, 230, 230), 0f, 0f, 1f, Random.nextBytes(20), "format")

        enableFaceTracking()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(smallFrame, smallFrame)

        // 160px clears the floor, so only the proportional rules have a say
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
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.process(smallFrame, smallFrame)

        assertThat(viewModel.state.value.feedback).isEqualTo(LiveFeedbackState.Feedback.TOO_FAR)
    }

    @Test
    fun `tracking - the configured image cap is the one the crop is taken with`() = runTest {
        every { faceDetector.analyze(frame, any(), any()) } returns trackedFace()

        enableFaceTracking(maxImageSizePx = 512)
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        verify { cropToFaceSquare.invoke(any(), any(), 512) }
    }

    /** A mocked frame of [size] square pixels, for the rules that count pixels rather than ratios. */
    private fun previewFrame(size: Int) = mockk<Bitmap>(relaxed = true) {
        every { width } returns size
        every { height } returns size
    }

    /** Turns on the experimental whole-preview tracking behaviour for a single test. */
    private fun enableFaceTracking(
        minFaceSizePx: Int = FaceTrackingConfiguration.DISABLED.minFaceSizePx,
        maxImageSizePx: Int = FaceTrackingConfiguration.DISABLED.maxImageSizePx,
    ) {
        every { getFaceTrackingConfiguration.invoke(any()) } returns
            FaceTrackingConfiguration(enabled = true, minFaceSizePx = minFaceSizePx, maxImageSizePx = maxImageSizePx)
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

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)

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
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1)
        viewModel.startCapture()
        viewModel.process(frame, frame)

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
