package com.simprints.face.capture.livefeedback

import android.graphics.Rect
import android.graphics.RectF
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.face.FixtureGenerator.faceCaptureBiometricsEvent1
import com.simprints.face.FixtureGenerator.faceCaptureBiometricsEvent2
import com.simprints.face.FixtureGenerator.faceCaptureBiometricsEvent3
import com.simprints.face.FixtureGenerator.faceCaptureEvent1
import com.simprints.face.FixtureGenerator.faceCaptureEvent2
import com.simprints.face.FixtureGenerator.faceCaptureEvent3
import com.simprints.face.FixtureGenerator.getFace
import com.simprints.face.capture.FaceCaptureViewModel
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
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LiveFeedbackFragmentViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val mainVM: FaceCaptureViewModel = mockk(relaxUnitFun = true) {
        every { attemptNumber } returns 0
        every { samplesToCapture } returns 2
    }
    private val qualityThreshold = -1f
    private val faceDetector: FaceDetector = mockk()
    private val frameProcessor: FrameProcessor = mockk()
    private val faceSessionEventsManager: FaceSessionEventsManager = mockk(relaxUnitFun = true)
    private val faceTimeHelper: FaceTimeHelper = mockk() {
        every { now() } returns 0
    }
    private val viewModel = LiveFeedbackFragmentViewModel(
        mainVM,
        faceDetector,
        frameProcessor,
        qualityThreshold,
        faceSessionEventsManager,
        faceTimeHelper
    )

    private val rectF: RectF = mockk()
    private val frame: Frame = mockk()
    private val size: Size = mockk()

    @Test
    fun `process valid face correctly`() = testCoroutineRule.runBlockingTest {
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

        viewModel.process(frame, rectF, size)

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
    fun `process invalid faces correctly`() = testCoroutineRule.runBlockingTest {
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

        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.TOOFAR)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.TOOCLOSE)
            assertThat(it[2]?.status).isEqualTo(FaceDetection.Status.NOFACE)
        }

        verify(exactly = 0) { faceSessionEventsManager.addEventInBackground(any()) }
    }

    @Test
    fun `save all valid captures without fallback image`() = testCoroutineRule.runBlockingTest {
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
        val mainCapturedDetections: CapturingSlot<List<FaceDetection>> = slot()
        every { mainVM.captureFinished(capture(mainCapturedDetections)) } just Runs
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()

        viewModel.process(frame, rectF, size)
        viewModel.startCapture()
        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)

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

        with(mainCapturedDetections.captured) {
            assertThat(size).isEqualTo(2)
            assertThat(get(0).face).isEqualTo(validFace)
            assertThat(get(0).isFallback).isEqualTo(false)
            assertThat(get(1).face).isEqualTo(validFace)
            assertThat(get(1).isFallback).isEqualTo(false)
        }

        verifySequence {
            faceSessionEventsManager.addEventInBackground(match {
                with(it as FaceFallbackCaptureEvent) {
                    assertThat(startTime).isEqualTo(0)
                    assertThat(endTime).isEqualTo(1)
                }
                true
            })
            faceSessionEventsManager.addEvent(match {
                with(faceCaptureEvent1) {
                    assertThat(startTime).isEqualTo(2)
                    assertThat(endTime).isEqualTo(3)
                    assertThat(isFallback).isEqualTo(false)
                    assertThat(attemptNb).isEqualTo(0)
                    assertThat(qualityThreshold).isEqualTo(this@LiveFeedbackFragmentViewModelTest.qualityThreshold)
                    assertThat(result).isEqualTo(FaceCaptureEvent.Result.VALID)
                    assertThat(eventFace).isNotNull()
                    eventFace?.let {
                        assertThat(it.quality).isEqualTo(validFace.quality)
                        assertThat(it.yaw).isEqualTo(validFace.yaw)
                        assertThat(it.yaw).isEqualTo(validFace.roll)
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match {
                with(faceCaptureBiometricsEvent1) {
                    assertThat(startTime).isEqualTo(2)
                    assertThat(endTime).isEqualTo(0)
                    assertThat(qualityThreshold).isEqualTo(this@LiveFeedbackFragmentViewModelTest.qualityThreshold)
                    assertThat(result).isEqualTo(FaceCaptureBiometricsEvent.Result.VALID)
                    assertThat(eventFace).isNotNull()
                    eventFace?.let {
                        assertThat(it.format).isEqualTo(FaceTemplateFormat.MOCK)
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match {
                with(faceCaptureEvent2) {
                    assertThat(startTime).isEqualTo(4)
                    assertThat(endTime).isEqualTo(5)
                    assertThat(isFallback).isEqualTo(false)
                    assertThat(attemptNb).isEqualTo(0)
                    assertThat(qualityThreshold).isEqualTo(this@LiveFeedbackFragmentViewModelTest.qualityThreshold)
                    assertThat(result).isEqualTo(FaceCaptureEvent.Result.VALID)
                    assertThat(eventFace).isNotNull()
                    eventFace?.let {
                        assertThat(it.quality).isEqualTo(validFace.quality)
                        assertThat(it.yaw).isEqualTo(validFace.yaw)
                        assertThat(it.yaw).isEqualTo(validFace.roll)
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match {
                with(faceCaptureBiometricsEvent2) {
                    assertThat(startTime).isEqualTo(4)
                    assertThat(endTime).isEqualTo(0)
                    assertThat(qualityThreshold).isEqualTo(this@LiveFeedbackFragmentViewModelTest.qualityThreshold)
                    assertThat(result).isEqualTo(FaceCaptureBiometricsEvent.Result.VALID)
                    assertThat(eventFace).isNotNull()
                    eventFace?.let {
                        assertThat(it.format).isEqualTo(FaceTemplateFormat.MOCK)
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match {
                with(faceCaptureEvent3) {
                    assertThat(startTime).isEqualTo(0)
                    assertThat(endTime).isEqualTo(1)
                    assertThat(isFallback).isEqualTo(true)
                    assertThat(attemptNb).isEqualTo(0)
                    assertThat(qualityThreshold).isEqualTo(this@LiveFeedbackFragmentViewModelTest.qualityThreshold)
                    assertThat(result).isEqualTo(FaceCaptureEvent.Result.VALID)
                    assertThat(eventFace).isNotNull()
                    eventFace?.let {
                        assertThat(it.quality).isEqualTo(validFace.quality)
                        assertThat(it.yaw).isEqualTo(validFace.yaw)
                        assertThat(it.yaw).isEqualTo(validFace.roll)
                    }
                }
                true
            })
            faceSessionEventsManager.addEvent(match {
                with(faceCaptureBiometricsEvent3) {
                    assertThat(startTime).isEqualTo(0)
                    assertThat(endTime).isEqualTo(0)
                    assertThat(qualityThreshold).isEqualTo(this@LiveFeedbackFragmentViewModelTest.qualityThreshold)
                    assertThat(result).isEqualTo(FaceCaptureBiometricsEvent.Result.VALID)
                    assertThat(eventFace).isNotNull()
                    eventFace?.let {
                        assertThat(it.format).isEqualTo(FaceTemplateFormat.MOCK)
                    }
                }
                true
            })
        }
    }

    @Test
    fun `save at least one valid captures without fallback image`() =
        testCoroutineRule.runBlockingTest {
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
            val mainCapturedDetections: CapturingSlot<List<FaceDetection>> = slot()
            every { mainVM.captureFinished(capture(mainCapturedDetections)) } just Runs
            every { faceTimeHelper.now() } returnsMany (0..100L).toList()

            val currentDetectionObserver = viewModel.currentDetection.testObserver()
            val capturingStateObserver = viewModel.capturingState.testObserver()

            viewModel.process(frame, rectF, size)
            viewModel.startCapture()
            viewModel.process(frame, rectF, size)
            viewModel.process(frame, rectF, size)

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

            with(mainCapturedDetections.captured) {
                assertThat(size).isEqualTo(1)
                assertThat(get(0).face).isEqualTo(validFace)
                assertThat(get(0).isFallback).isEqualTo(false)
            }

            verify(exactly = 1) { faceSessionEventsManager.addEventInBackground(any()) }
            verify(exactly = 6) { faceSessionEventsManager.addEvent(any()) }
        }

    /**
     * This tests a case where the button turns green (a valid capture), the user clicked the button
     * but then moved the phone very fast and didn't capture anything
     */
    @Test
    fun `use fallback image if all captures are invalid`() = testCoroutineRule.runBlockingTest {
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
        val mainCapturedDetections: CapturingSlot<List<FaceDetection>> = slot()
        every { mainVM.captureFinished(capture(mainCapturedDetections)) } just Runs
        every { faceTimeHelper.now() } returnsMany (0..100L).toList()

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()

        // This means the button turned green, the user clicked and then moved the camera away
        viewModel.process(frame, rectF, size)
        viewModel.startCapture()
        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)

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

        with(mainCapturedDetections.captured[0]) {
            assertThat(face).isEqualTo(validFace)
            assertThat(isFallback).isEqualTo(true)
        }

        verify(exactly = 1) { faceSessionEventsManager.addEventInBackground(any()) }
        verify(exactly = 6) { faceSessionEventsManager.addEvent(any()) }
    }
}
