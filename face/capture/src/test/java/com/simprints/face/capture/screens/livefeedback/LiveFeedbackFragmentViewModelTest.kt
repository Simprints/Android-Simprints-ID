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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
internal class LiveFeedbackFragmentViewModelTest {
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
        every { isUsingAutoCapture.invoke(any()) } returns false
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns FaceConfiguration.SpoofCheckConfiguration.DISABLED

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
    fun `Process fallback image when valid face correctly but not started capture`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)

        val currentDetection = viewModel.currentDetection.testObserver()
        assertThat(currentDetection.observedValues.last()?.status).isEqualTo(FaceDetection.Status.VALID)

        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
    }

    @Test
    fun `Process valid face correctly`() = runTest {
        coEvery { faceDetector.analyze(frame) } returns getFace()

        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        val currentDetection = viewModel.currentDetection.testObserver()
        assertThat(currentDetection.observedValues.last()?.status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)

        coVerify { eventReporter.addCaptureEvents(any(), any(), any()) }
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
            badQuality,
            validFace,
            badQuality,
        )

        val detections = viewModel.currentDetection.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        detections.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.VALID)
        }
    }

    @Test
    fun `Save all valid captures without fallback image`() = runTest {
        val validFace: Face = getFace()
        every { faceDetector.analyze(frame) } returns validFace
        every { timeHelper.now() } returnsMany (0..100L).map { Timestamp(it) }

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 2, 0)
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)
        viewModel.process(frame, frame)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
            assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
        }

        capturingStateObserver.observedValues.let {
            assertThat(it[0]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
            assertThat(it[1]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
            assertThat(it[2]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        }

        assertThat(viewModel.userCaptures.size).isEqualTo(2)
        viewModel.userCaptures.let {
            with(it[0]) {
                assertThat(status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
                assertThat(face).isEqualTo(validFace)
                assertThat(isFallback).isEqualTo(false)
            }

            assertThat(it[1].isFallback).isEqualTo(false)
        }

        with(viewModel.sortedQualifyingCaptures) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0).face).isEqualTo(validFace)
            assertThat(get(0).isFallback).isEqualTo(false)
            assertThat(get(1).face).isEqualTo(validFace)
            assertThat(get(1).isFallback).isEqualTo(false)
        }

        coVerify { eventReporter.addFallbackCaptureEvent(any(), any()) }
        coVerify(exactly = 3) { eventReporter.addCaptureEvents(any(), any(), any()) }
    }

    @Test
    fun `Spoof check Recorded finishes capture regardless of spoof result`() = runTest {
        val validFace: Face = getFace()
        every {
            getSpoofCheckConfiguration.invoke(
                any(),
                any(),
            )
        } returns FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.RECORDED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.9f)

        val capturingStateObserver = viewModel.capturingState.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)

        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingStateObserver.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        assertThat(viewModel.sortedQualifyingCaptures.size).isEqualTo(1)
        assertThat(viewModel.sortedQualifyingCaptures[0].spoofCheckResult?.score).isEqualTo(0.9f)
    }

    @Test
    fun `Spoof check Enforced passed finishes capture`() = runTest {
        val validFace: Face = getFace()
        every { getSpoofCheckConfiguration.invoke(any(), any()) } returns
            FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.1f)

        val capturingStateObserver = viewModel.capturingState.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)

        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingStateObserver.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        assertThat(viewModel.sortedQualifyingCaptures.size).isEqualTo(1)
        assertThat(viewModel.sortedQualifyingCaptures[0].spoofCheckResult?.score).isEqualTo(0.1f)
    }

    @Test
    fun `Spoof check Enforced failed resets state`() = runTest {
        val validFace: Face = getFace()
        every {
            getSpoofCheckConfiguration.invoke(
                any(),
                any(),
            )
        } returns FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.9f)

        val capturingStateObserver = viewModel.capturingState.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)

        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingStateObserver.observedValues).contains(LiveFeedbackFragmentViewModel.CapturingState.VALIDATION_FAILED)
        assertThat(capturingStateObserver.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
        assertThat(viewModel.sortedQualifyingCaptures.size).isEqualTo(0)
        assertThat(viewModel.userCaptures.size).isEqualTo(0)
    }

    @Test
    fun `Spoof check Enforced failed max times finishes capture`() = runTest {
        val validFace: Face = getFace()
        every {
            getSpoofCheckConfiguration.invoke(
                any(),
                any(),
            )
        } returns FaceConfiguration.SpoofCheckConfiguration(FaceConfiguration.SpoofCheckMode.ENFORCED, 0.5f)
        every { faceDetector.analyze(frame) } returns validFace
        coEvery { faceDetector.spoofCheck(any()) } returns SpoofCheckResult(score = 0.9f)

        val capturingStateObserver = viewModel.capturingState.testObserver()
        viewModel.initAutoCapture()
        viewModel.initCapture(ModalitySdkType.SIM_FACE, 1, 0)

        // Attempt 1
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingStateObserver.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)

        // Attempt 2
        viewModel.process(frame, frame)
        viewModel.startCapture()
        viewModel.process(frame, frame)

        advanceUntilIdle()

        assertThat(capturingStateObserver.observedValues.last()).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
    }

    private fun getFace(
        rect: Rect = Rect(0, 0, 60, 60),
        quality: Float = 1f,
        yaw: Float = 0f,
        roll: Float = 0f,
    ) = Face(100, 100, rect, yaw, roll, quality, Random.nextBytes(20), "format")

    companion object {
        private const val QUALITY_THRESHOLD = -1f
    }
}
