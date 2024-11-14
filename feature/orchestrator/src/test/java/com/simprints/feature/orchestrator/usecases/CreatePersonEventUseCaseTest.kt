package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class CreatePersonEventUseCaseTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var eventRepository: SessionEventRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var useCase: CreatePersonEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(0L)

        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            every { id } returns "sessionId"
        }

        useCase = CreatePersonEventUseCase(eventRepository, timeHelper)
    }

    @Test
    fun `Does not create event if no biometric data`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf()

        useCase(listOf())

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `Create event if there is face biometric data`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf()

        useCase(listOf(FaceCaptureResult(listOf(createFaceCaptureResultItem()))))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                assertThat(it.payload.faceCaptureIds).isEqualTo(listOf(FACE_CAPTURE_ID))
            })
        }
    }

    @Test
    fun `Create event if there is fingerprint biometric data`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<FingerprintCaptureBiometricsEvent> {
                every { payload.id } returns FINGER_CAPTURE_ID
                every { payload.fingerprint.template } returns TEMPLATE
            },
        )

        useCase(listOf(FingerprintCaptureResult(listOf(createFingerprintCaptureResultItem()))))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                assertThat(it.payload.fingerprintCaptureIds).isEqualTo(listOf(FINGER_CAPTURE_ID))
            })
        }
    }

    @Test
    fun `Gets fingerprint from previous PersonCreationEvent (when present) if missing in current callout captures`() =
        runTest {
            coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
                mockk<PersonCreationEvent> {
                    every { payload.fingerprintCaptureIds } returns listOf(FINGER_CAPTURE_ID)
                    every { payload.fingerprintReferenceId } returns FINGER_REFERENCE_ID
                },
            )

            useCase(listOf(FaceCaptureResult(listOf(createFaceCaptureResultItem()))))

            coVerify {
                eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                    assertThat(it.payload.faceCaptureIds).isEqualTo(listOf(FACE_CAPTURE_ID))
                    assertThat(it.payload.fingerprintCaptureIds).isEqualTo(listOf(FINGER_CAPTURE_ID))
                    assertThat(it.payload.fingerprintReferenceId).isEqualTo(FINGER_REFERENCE_ID)
                })
            }
        }

    @Test
    fun `Gets face from previous PersonCreationEvent (when present) if missing in current callout captures`() =
        runTest {
            coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
                mockk<PersonCreationEvent> {
                    every { payload.faceCaptureIds } returns listOf(FACE_CAPTURE_ID)
                    every { payload.faceReferenceId } returns FACE_REFERENCE_ID
                },
            )

            useCase(listOf(FingerprintCaptureResult(listOf(createFingerprintCaptureResultItem()))))

            coVerify {
                eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                    assertThat(it.payload.fingerprintCaptureIds).isEqualTo(listOf(FINGER_CAPTURE_ID))
                    assertThat(it.payload.faceCaptureIds).isEqualTo(listOf(FACE_CAPTURE_ID))
                    assertThat(it.payload.faceReferenceId).isEqualTo(FACE_REFERENCE_ID)
                })
            }
        }

    @Test
    fun `Uses face from latest PersonCreationEvent (if more than one) if missing in current callout captures`() =
        runTest {
            coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
                mockk<PersonCreationEvent> {
                    every { payload.faceCaptureIds } returns listOf(FACE_CAPTURE_ID)
                    every { payload.faceReferenceId } returns FACE_REFERENCE_ID
                    every { payload.createdAt } returns Timestamp(2L)
                },
                mockk<PersonCreationEvent> {
                    every { payload.faceCaptureIds } returns listOf("anotherFaceCaptureId")
                    every { payload.faceReferenceId } returns "anotherFaceReferenceId"
                    every { payload.createdAt } returns Timestamp(1L)
                },
            )

            useCase(listOf(FingerprintCaptureResult(listOf(createFingerprintCaptureResultItem()))))

            coVerify {
                eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                    assertThat(it.payload.fingerprintCaptureIds).isEqualTo(listOf(FINGER_CAPTURE_ID))
                    assertThat(it.payload.faceCaptureIds).isEqualTo(listOf(FACE_CAPTURE_ID))
                    assertThat(it.payload.faceReferenceId).isEqualTo(FACE_REFERENCE_ID)
                })
            }
        }

    private fun createFingerprintCaptureResultItem() = FingerprintCaptureResult.Item(
        captureEventId = FINGER_CAPTURE_ID,
        identifier = IFingerIdentifier.RIGHT_THUMB,
        sample = FingerprintCaptureResult.Sample(
            IFingerIdentifier.RIGHT_THUMB,
            TEMPLATE.toByteArray(),
            0,
            null,
            "format"
        )
    )


    private fun createFaceCaptureResultItem() =
        FaceCaptureResult.Item(
            captureEventId = FACE_CAPTURE_ID,
            index = 0,
            sample = FaceCaptureResult.Sample(FACE_CAPTURE_ID, TEMPLATE.toByteArray(), null, "format")
        )


    companion object {

        private const val TEMPLATE = "template"
        private const val FINGER_CAPTURE_ID = "fingerprintCaptureId"
        private const val FINGER_REFERENCE_ID = "fingerReferenceId"
        private const val FACE_CAPTURE_ID = "faceCaptureId"
        private const val FACE_REFERENCE_ID = "faceReferenceId"
    }
}
