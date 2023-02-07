package com.simprints.face.capture.livefeedback

import android.graphics.Rect
import android.graphics.RectF
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.face.FixtureGenerator.getFace
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.Event
import com.simprints.face.controllers.core.events.model.FaceCaptureBiometricsEvent
import com.simprints.face.controllers.core.events.model.FaceCaptureEvent
import com.simprints.face.controllers.core.events.model.FaceFallbackCaptureEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.face.models.PreviewFrame
import com.simprints.face.models.Size
import com.simprints.infra.config.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class LiveFeedbackFragmentViewModelTest {

    companion object {
        private const val QUALITY_THRESHOLD = -1
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val configManager: ConfigManager = mockk {
        coEvery { getProjectConfiguration() } returns mockk {
            every { face } returns mockk {
                every { qualityThreshold } returns QUALITY_THRESHOLD
            }
        }
    }
    private val faceDetector: FaceDetector = mockk()
    private val frameProcessor: FrameProcessor = mockk()
    private val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxUnitFun = true)
    private val faceTimeHelper: FaceTimeHelper = mockk {
        every { now() } returns 0
    }
    private val viewModel = LiveFeedbackFragmentViewModel(
        faceDetector,
        frameProcessor,
        configManager,
        faceSessionEventsManager,
        faceTimeHelper,
    )

    private val rectF: RectF = mockk()
    private val frame: Frame = mockk()
    private val size: Size = mockk()

    @Before
    fun setup() {
        mockkStatic(UUID::class)
    }

    @Test
    fun `process valid face correctly`() = runTest {
        val previewFrameMock: PreviewFrame = mockk()
        val validFace: Face = getFace()
        every {
            frameProcessor.previewFrameFrom(
                any(),
                any(),
                any(),
                any()
            )
        } returns previewFrameMock
        coEvery { faceDetector.analyze(previewFrameMock) } returns validFace
        val eventCapture: CapturingSlot<Event> = slot()
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()
        every { faceSessionEventsManager.addEventInBackground(capture(eventCapture)) } just Runs

        val currentDetectionObserver = viewModel.currentDetection.testObserver()

        viewModel.process(frame, rectF, size, 2, 0)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
        }

        with(eventCapture.captured) {
            assertThat(startTime).isEqualTo(0)
            assertThat(endTime).isEqualTo(1)
            assertThat(this).isInstanceOf(FaceFallbackCaptureEvent::class.java)
        }

        verify(atMost = 1) { faceSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun `process invalid faces correctly`() = runTest {
        val previewFrameMock: PreviewFrame = mockk()
        val smallFace: Face = getFace(Rect(0, 0, 30, 30))
        val bigFace: Face = getFace(Rect(0, 0, 80, 80))
        val noFace = null

        every {
            frameProcessor.previewFrameFrom(
                any(),
                any(),
                any(),
                any()
            )
        } returns previewFrameMock
        coEvery { faceDetector.analyze(previewFrameMock) } returnsMany listOf(
            smallFace,
            bigFace,
            noFace
        )

        val currentDetectionObserver = viewModel.currentDetection.testObserver()

        viewModel.process(frame, rectF, size, 2, 0)
        viewModel.process(frame, rectF, size, 2, 0)
        viewModel.process(frame, rectF, size, 2, 0)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.TOOFAR)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.TOOCLOSE)
            assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.NOFACE)
        }

        verify(exactly = 0) { faceSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun `save all valid captures without fallback image`() = runTest {
        val faceDetectionId1 = "24d5d5da-c950-4da5-bfc6-99419a22bb08"
        val faceFallbackId1 = "76784a2b-7128-4cc6-8f15-b157e2000d8b"
        val faceCaptureId1 = "f0339487-5878-4ceb-8f33-fdbfaadcafe8"
        val faceCaptureBiometricId1 = "45d92010-bc36-48aa-8061-d395de8002b1"
        val faceDetectionId2 = "839abd77-a8ce-46b5-b9a0-608f512ddae0"
        val faceCaptureId2 = "299fc413-4cf4-4a07-9463-185296f8e907"
        val faceCaptureBiometricId2 = "13d52136-4709-45fd-9ea3-2c0c9896119a"
        val faceDetectionId3 = "515b80e7-fdec-4acd-91b8-c5afa901d790"
        val faceCaptureId3 = "8a2e1bc8-32c4-4318-a111-a9c14cab69fd"
        val faceCaptureBiometricId3 = "6b883ffe-8227-47e9-af47-07abc09ae395"

        val previewFrameMock: PreviewFrame = mockk()
        val validFace: Face = getFace()
        every {
            frameProcessor.previewFrameFrom(
                any(),
                any(),
                any(),
                any()
            )
        } returns previewFrameMock
        coEvery { faceDetector.analyze(previewFrameMock) } returns validFace
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()
        every { UUID.randomUUID() } returnsMany listOf(
            UUID.fromString(faceDetectionId1),
            UUID.fromString(faceFallbackId1),
            UUID.fromString(faceDetectionId2),
            UUID.fromString(faceCaptureId3),
            UUID.fromString(faceCaptureId2),
            UUID.fromString(faceCaptureBiometricId2),
            UUID.fromString(faceDetectionId3),
            UUID.fromString(faceCaptureBiometricId3),
            UUID.fromString(faceCaptureId1),
            UUID.fromString(faceCaptureBiometricId1),
        )

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()

        viewModel.process(frame, rectF, size, 2, 0)
        viewModel.startCapture()
        viewModel.process(frame, rectF, size, 2, 0)
        viewModel.process(frame, rectF, size, 2, 0)

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

        assertThat(viewModel.fallbackCapture).isNotNull()

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

        verifyAll {
            faceSessionEventsManager.addEventInBackground(match {
                with(it as FaceFallbackCaptureEvent) {
                    assertThat(startTime).isEqualTo(0)
                    assertThat(endTime).isEqualTo(1)
                }
                true
            })
            faceSessionEventsManager.addEvent(match { event ->
                if (event.id == faceCaptureId2) {
                    with(event as FaceCaptureEvent) {
                        assertThat(payloadId).isEqualTo(currentDetectionObserver.observedValues[1]?.id)
                        assertThat(startTime).isEqualTo(2)
                        assertThat(endTime).isEqualTo(3)
                        assertThat(isFallback).isEqualTo(false)
                        assertThat(attemptNb).isEqualTo(0)
                        assertThat(qualityThreshold).isEqualTo(QUALITY_THRESHOLD)
                        assertThat(result).isEqualTo(FaceCaptureEvent.Result.VALID)
                        assertThat(eventFace).isNotNull()
                        eventFace?.let {
                            assertThat(it.quality).isEqualTo(validFace.quality)
                            assertThat(it.yaw).isEqualTo(validFace.yaw)
                            assertThat(it.yaw).isEqualTo(validFace.roll)
                        }
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match { event ->
                if (event.id == faceCaptureBiometricId2) {
                    with(event as FaceCaptureBiometricsEvent) {
                        assertThat(payloadId).isEqualTo(currentDetectionObserver.observedValues[1]?.id)
                        assertThat(startTime).isEqualTo(2)
                        assertThat(endTime).isEqualTo(0)
                        assertThat(eventFace).isNotNull()
                        assertThat(eventFace.format).isEqualTo(FaceTemplateFormat.MOCK)
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match { event ->
                if (event.id == faceCaptureId3) {
                    with(event as FaceCaptureEvent) {
                        assertThat(payloadId).isEqualTo(currentDetectionObserver.observedValues[2]?.id)
                        assertThat(startTime).isEqualTo(4)
                        assertThat(endTime).isEqualTo(5)
                        assertThat(isFallback).isEqualTo(false)
                        assertThat(attemptNb).isEqualTo(0)
                        assertThat(qualityThreshold).isEqualTo(QUALITY_THRESHOLD)
                        assertThat(result).isEqualTo(FaceCaptureEvent.Result.VALID)
                        assertThat(eventFace).isNotNull()
                        eventFace?.let {
                            assertThat(it.quality).isEqualTo(validFace.quality)
                            assertThat(it.yaw).isEqualTo(validFace.yaw)
                            assertThat(it.yaw).isEqualTo(validFace.roll)
                        }
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match { event ->
                if (event.id == faceCaptureBiometricId3) {
                    with(event as FaceCaptureBiometricsEvent) {
                        assertThat(payloadId).isEqualTo(currentDetectionObserver.observedValues[2]?.id)
                        assertThat(startTime).isEqualTo(4)
                        assertThat(endTime).isEqualTo(0)
                        assertThat(eventFace).isNotNull()
                        eventFace.let {
                            assertThat(it.format).isEqualTo(FaceTemplateFormat.MOCK)
                        }
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match { event ->
                if (event.id == faceCaptureId1) {
                    with(event as FaceCaptureEvent) {
                        assertThat(payloadId).isEqualTo(currentDetectionObserver.observedValues[0]?.id)
                        assertThat(startTime).isEqualTo(0)
                        assertThat(endTime).isEqualTo(1)
                        assertThat(isFallback).isEqualTo(true)
                        assertThat(attemptNb).isEqualTo(0)
                        assertThat(qualityThreshold).isEqualTo(QUALITY_THRESHOLD)
                        assertThat(result).isEqualTo(FaceCaptureEvent.Result.VALID)
                        assertThat(eventFace).isNotNull()
                        eventFace?.let {
                            assertThat(it.quality).isEqualTo(validFace.quality)
                            assertThat(it.yaw).isEqualTo(validFace.yaw)
                            assertThat(it.yaw).isEqualTo(validFace.roll)
                        }
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match { event ->
                if (event.id == faceCaptureBiometricId1) {
                    with(event as FaceCaptureBiometricsEvent) {
                        assertThat(payloadId).isEqualTo(currentDetectionObserver.observedValues[0]?.id)
                        assertThat(startTime).isEqualTo(0)
                        assertThat(endTime).isEqualTo(0)
                        assertThat(eventFace).isNotNull()
                        assertThat(eventFace.format).isEqualTo(FaceTemplateFormat.MOCK)

                    }
                }
                true
            })
        }
    }

    @Test
    fun `save at least one valid captures without fallback image`() =
        runTest {
            val previewFrameMock: PreviewFrame = mockk()
            val validFace: Face = getFace()
            val noFace = null
            every {
                frameProcessor.previewFrameFrom(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns previewFrameMock
            coEvery { faceDetector.analyze(previewFrameMock) } returnsMany listOf(
                validFace,
                validFace,
                noFace
            )
            every { faceTimeHelper.now() } returnsMany (0..100L).toList()

            val currentDetectionObserver = viewModel.currentDetection.testObserver()
            val capturingStateObserver = viewModel.capturingState.testObserver()

            viewModel.process(frame, rectF, size, 2, 0)
            viewModel.startCapture()
            viewModel.process(frame, rectF, size, 2, 0)
            viewModel.process(frame, rectF, size, 2, 0)

            currentDetectionObserver.observedValues.let {
                assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
                assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
                assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.NOFACE)
            }

            capturingStateObserver.observedValues.let {
                assertThat(it[0]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
                assertThat(it[1]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
                assertThat(it[2]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
            }

            assertThat(viewModel.fallbackCapture).isNotNull()

            assertThat(viewModel.userCaptures.size).isEqualTo(2)
            viewModel.userCaptures.let {
                with(it[0]) {
                    assertThat(status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
                    assertThat(face).isEqualTo(validFace)
                    assertThat(isFallback).isEqualTo(false)
                }

                with(it[1]) {
                    assertThat(status).isEqualTo(FaceDetection.Status.NOFACE)
                    assertThat(isFallback).isEqualTo(false)
                }
            }

            with(viewModel.sortedQualifyingCaptures) {
                assertThat(size).isEqualTo(1)
                assertThat(get(0).face).isEqualTo(validFace)
                assertThat(get(0).isFallback).isEqualTo(false)
            }

            verify(exactly = 1) { faceSessionEventsManager.addEventInBackground(any()) }
            verify(exactly = 5) { faceSessionEventsManager.addEvent(any()) }
        }

    /**
     * This tests a case where the button turns green (a valid capture), the user clicked the button
     * but then moved the phone very fast and didn't capture anything
     */
    @Test
    fun `use fallback image if all captures are invalid`() = runTest {
        val previewFrameMock: PreviewFrame = mockk()
        val validFace: Face = getFace()
        val tooFarFace = getFace(Rect(0, 0, 30, 30))
        val noFace = null
        every {
            frameProcessor.previewFrameFrom(
                any(),
                any(),
                any(),
                any()
            )
        } returns previewFrameMock
        coEvery { faceDetector.analyze(previewFrameMock) } returnsMany listOf(
            validFace,
            tooFarFace,
            noFace
        )
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()

        // This means the button turned green, the user clicked and then moved the camera away
        viewModel.process(frame, rectF, size, 2, 0)
        viewModel.startCapture()
        viewModel.process(frame, rectF, size, 2, 0)
        viewModel.process(frame, rectF, size, 2, 0)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.TOOFAR)
            assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.NOFACE)
        }

        capturingStateObserver.observedValues.let {
            assertThat(it[0]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
            assertThat(it[1]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
            assertThat(it[2]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        }

        assertThat(viewModel.userCaptures.size).isEqualTo(2)
        with(viewModel.userCaptures[0]) {
            assertThat(status).isEqualTo(FaceDetection.Status.TOOFAR)
            assertThat(face).isEqualTo(tooFarFace)
        }

        with(viewModel.sortedQualifyingCaptures[0]) {
            assertThat(face).isEqualTo(validFace)
            assertThat(isFallback).isEqualTo(true)
        }

        verify(exactly = 1) { faceSessionEventsManager.addEventInBackground(any()) }
        verify(exactly = 4) { faceSessionEventsManager.addEvent(any()) }
    }
}
