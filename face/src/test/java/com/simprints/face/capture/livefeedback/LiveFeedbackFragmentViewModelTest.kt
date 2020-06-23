package com.simprints.face.capture.livefeedback

import android.graphics.Rect
import android.graphics.RectF
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.face.FixtureGenerator.getFace
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.uicomponents.models.PreviewFrame
import com.simprints.uicomponents.models.Size
import io.mockk.*
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
        every { samplesToCapture } returns 2
    }
    private val qualityThreshold = -1f
    private val faceDetector: FaceDetector = mockk()
    private val frameProcessor: FrameProcessor = mockk()
    private val viewModel =
        LiveFeedbackFragmentViewModel(mainVM, faceDetector, frameProcessor, qualityThreshold)

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

        val currentDetectionObserver = viewModel.currentDetection.testObserver()

        viewModel.process(frame, rectF, size)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
        }
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
    }

    @Test
    fun `save all valid captures correctly`() = testCoroutineRule.runBlockingTest {
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

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()

        viewModel.startCapture()
        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
        }

        capturingStateObserver.observedValues.let {
            assertThat(it[0]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
            assertThat(it[1]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
            assertThat(it[2]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        }

        assertThat(viewModel.userCaptures.size).isEqualTo(2)
        viewModel.userCaptures.let {
            assertThat(it[0].status).isEqualTo(FaceDetection.Status.VALID_CAPTURING)
            assertThat(it[0].face).isEqualTo(validFace)
        }
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
        }

    /**
     * This tests a case where the button turns green (a valid capture), the user clicked the button
     * but then moved the phone very fast and didn't capture anything
     */
    @Test
    fun `use fallback image if all captures are invalid`() = testCoroutineRule.runBlockingTest {
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
            noFace,
            noFace
        )
        val mainCapturedDetections: CapturingSlot<List<FaceDetection>> = slot()
        every { mainVM.captureFinished(capture(mainCapturedDetections)) } just Runs

        val currentDetectionObserver = viewModel.currentDetection.testObserver()
        val capturingStateObserver = viewModel.capturingState.testObserver()

        // This means the button turned green, the user clicked and then moved the camera away
        viewModel.process(frame, rectF, size)
        viewModel.startCapture()
        viewModel.process(frame, rectF, size)
        viewModel.process(frame, rectF, size)

        currentDetectionObserver.observedValues.let {
            assertThat(it[0]?.status).isEqualTo(FaceDetection.Status.VALID)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.NOFACE)
            assertThat(it[1]?.status).isEqualTo(FaceDetection.Status.NOFACE)
        }

        capturingStateObserver.observedValues.let {
            assertThat(it[0]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.NOT_STARTED)
            assertThat(it[1]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.CAPTURING)
            assertThat(it[2]).isEqualTo(LiveFeedbackFragmentViewModel.CapturingState.FINISHED)
        }

        assertThat(viewModel.userCaptures.size).isEqualTo(2)
        with(viewModel.userCaptures[0]) {
            assertThat(status).isEqualTo(FaceDetection.Status.NOFACE)
            assertThat(face).isEqualTo(noFace)
        }

        with(mainCapturedDetections.captured[0]) {
            assertThat(face).isEqualTo(validFace)
            assertThat(isFallback).isEqualTo(true)
        }
    }

}
