package com.simprints.face.capture.usecases

import android.graphics.Rect
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SimpleCaptureEventReporterTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var encodingUtils: EncodingUtils

    private lateinit var reporter: SimpleCaptureEventReporter

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(1L)

        reporter = SimpleCaptureEventReporter(
            timeHelper,
            eventRepository,
            encodingUtils,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Adds face onboarding event`() = runTest {
        reporter.addOnboardingCompleteEvent(Timestamp(1L))
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceOnboardingCompleteEvent::class.java)
                },
            )
        }
    }

    @Test
    fun `Adds capture confirmation continued event`() = runTest {
        reporter.addCaptureConfirmationEvent(Timestamp(1L), true)
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureConfirmationEvent::class.java)
                    assertThat((it.payload as FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload).result).isEqualTo(
                        FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE,
                    )
                },
            )
        }
    }

    @Test
    fun `Adds capture confirmation restarted event`() = runTest {
        reporter.addCaptureConfirmationEvent(Timestamp(1L), false)
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureConfirmationEvent::class.java)
                    assertThat((it.payload as FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload).result).isEqualTo(
                        FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.RECAPTURE,
                    )
                },
            )
        }
    }

    @Test
    fun `Adds fallback capture event`() = runTest {
        reporter.addFallbackCaptureEvent(Timestamp(1L), Timestamp(1L))
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceFallbackCaptureEvent::class.java)
                },
            )
        }
    }

    @Test
    fun `Adds capture and biometric event for valid detection`() = runTest {
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.VALID), 1, 0.5f)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureEvent::class.java)
                },
            )
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureBiometricsEvent::class.java)
                },
            )
        }
    }

    @Test
    fun `Adds capture and biometric event for valid_capturing detection`() = runTest {
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.VALID_CAPTURING), 1, 0.5f)

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureEvent::class.java)
                },
            )
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureBiometricsEvent::class.java)
                },
            )
        }
    }

    @Test
    fun `Adds capture and no biometric event for invalid detections`() = runTest {
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.NOFACE), 1, 0.5f)
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.OFFYAW), 1, 0.5f)
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.OFFROLL), 1, 0.5f)
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.TOOCLOSE), 1, 0.5f)
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.TOOFAR), 1, 0.5f)
        reporter.addCaptureEvents(getDetection(FaceDetection.Status.BAD_QUALITY), 1, 0.5f)

        coVerify(exactly = 6) {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureEvent::class.java)
                },
            )
        }
        coVerify(exactly = 0) {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(FaceCaptureBiometricsEvent::class.java)
                },
            )
        }
    }

    @Test
    fun `Adds capture event with correct auto-capture flag`() = runTest {
        listOf(true, false).forEach { isAutoCapture ->
            reporter.addCaptureEvents(getDetection(FaceDetection.Status.VALID), 1, 0.5f, isAutoCapture)
            coVerify {
                eventRepository.addOrUpdateEvent(
                    withArg {
                        assertThat(it).isInstanceOf(FaceCaptureEvent::class.java)
                        assertThat((it.payload as FaceCaptureEvent.FaceCapturePayload).isAutoCapture).isEqualTo(isAutoCapture)
                    },
                )
            }
        }
    }

    @Test
    fun `Adds reference creation event`() = runTest {
        reporter.addBiometricReferenceCreationEvents("id", listOf("id1", "id2", "id3"))
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(BiometricReferenceCreationEvent::class.java)
                    assertThat((it.payload as BiometricReferenceCreationPayload).modality)
                        .isEqualTo(BiometricReferenceCreationEvent.BiometricReferenceModality.FACE)
                },
            )
        }
    }

    private fun getDetection(status: FaceDetection.Status) =
        FaceDetection(mockk(), getFace(), status, mockk(), Timestamp(1L), false, "id", Timestamp(1L))

    private fun getFace() = Face(
        100,
        100,
        Rect(0, 0, 0, 0),
        0f,
        0f,
        0f,
        byteArrayOf(),
        "",
    )
}
