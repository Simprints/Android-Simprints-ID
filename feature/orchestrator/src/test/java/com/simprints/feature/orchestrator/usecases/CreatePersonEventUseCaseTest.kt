package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreatePersonEventUseCaseTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var encodingUtils: EncodingUtils

    private lateinit var useCase: CreatePersonEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns 0L
        every { encodingUtils.byteArrayToBase64(any()) } returns TEMPLATE

        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns mockk {
            every { id } returns "sessionId"
        }
        coEvery { eventRepository.observeEventsFromSession(any()) }

        useCase = CreatePersonEventUseCase(eventRepository, timeHelper, encodingUtils)
    }

    @Test
    fun `Does not create event if has person creation in session`() = runTest {
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            mockk<FingerprintCaptureBiometricsEvent>(),
            mockk<FaceCaptureBiometricsEvent>(),
            mockk<PersonCreationEvent>(),
        )

        useCase(listOf())

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `Does not create event if no biometric data`() = runTest {
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            mockk<FingerprintCaptureEvent>(),
            mockk<FaceCaptureEvent>(),
        )

        useCase(listOf())

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `Create event if there is face biometric data`() = runTest {
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            mockk<FaceCaptureBiometricsEvent> {
                every { payload.id } returns "eventFaceId1"
                every { payload.face.template } returns TEMPLATE
            },
        )

        useCase(listOf(FaceCaptureResult(listOf(createFaceCaptureResultItem()))))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                assertThat(it.payload.faceCaptureIds).isEqualTo(listOf("eventFaceId1"))
            })
        }
    }

    @Test
    fun `Create event if there is fingerprint biometric data`() = runTest {
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            mockk<FingerprintCaptureBiometricsEvent> {
                every { payload.id } returns "eventFinger1"
                every { payload.fingerprint.template } returns TEMPLATE
            },
        )

        useCase(listOf(FingerprintCaptureResult(listOf(createFingerprintCaptureResultItem()))))

        coVerify {
            eventRepository.addOrUpdateEvent(withArg<PersonCreationEvent> {
                assertThat(it.payload.fingerprintCaptureIds).isEqualTo(listOf("eventFinger1"))
            })
        }
    }

    private fun createFingerprintCaptureResultItem() = FingerprintCaptureResult.Item(
        IFingerIdentifier.RIGHT_THUMB,
        FingerprintCaptureResult.Sample(IFingerIdentifier.RIGHT_THUMB, byteArrayOf(), 0, null, "format")
    )

    private fun createFaceCaptureResultItem() =
        FaceCaptureResult.Item(0, FaceCaptureResult.Sample("faceId", byteArrayOf(), null, "format"))


    companion object {

        const val TEMPLATE = "template"
    }
}
