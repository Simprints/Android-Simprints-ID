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
    fun `Does not create event if has person creation in session`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<PersonCreationEvent>(),
        )

        useCase(listOf())

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
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
                assertThat(it.payload.faceCaptureIds).isEqualTo(listOf(FACE_ID))
            })
        }
    }

    @Test
    fun `Create event if there is fingerprint biometric data`() = runTest {
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(
            mockk<FingerprintCaptureBiometricsEvent> {
                every { payload.id } returns FINGER_ID
                every { payload.fingerprint.template } returns TEMPLATE
            },
        )

        useCase(listOf(FingerprintCaptureResult(listOf(createFingerprintCaptureResultItem()))))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                assertThat(it.payload.fingerprintCaptureIds).isEqualTo(listOf(FINGER_ID))
            })
        }
    }

    private fun createFingerprintCaptureResultItem() = FingerprintCaptureResult.Item(
        captureEventId = FINGER_ID,
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
            captureEventId = FACE_ID,
            index = 0,
            sample = FaceCaptureResult.Sample(FACE_ID, TEMPLATE.toByteArray(), null, "format")
        )


    companion object {

        private const val TEMPLATE = "template"
        private const val FINGER_ID = "eventFinger1"
        private const val FACE_ID = "eventFinger1"
    }
}
