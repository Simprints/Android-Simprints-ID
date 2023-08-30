package com.simprints.id.orchestrator

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.sampledata.FACE_TEMPLATE_FORMAT
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import com.simprints.infra.events.sampledata.createFaceCaptureBiometricsEvent
import com.simprints.infra.events.sampledata.createFingerprintCaptureBiometricsEvent
import com.simprints.infra.events.sampledata.createPersonCreationEvent
import com.simprints.infra.events.sampledata.createSessionCaptureEvent
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class PersonCreationEventHelperImplTest {

    private val currentSession = createSessionCaptureEvent()

    private val fingerprintCaptureBiometricsEvent =
        createFingerprintCaptureBiometricsEvent().let { it.copy(labels = it.labels.copy(sessionId = currentSession.id)) }

    private val faceCaptureBiometricsEvent =
        createFaceCaptureBiometricsEvent().let { it.copy(labels = it.labels.copy(sessionId = currentSession.id)) }

    private val fingerprintSample = FingerprintCaptureSample(
        Finger.LEFT_THUMB,
        templateQualityScore = 10,
        template = EncodingUtilsImplForTests.base64ToBytes(
            "sometemplate"
        ),
        format = "ISO_19794_2"
    )

    private val faceSample = FaceCaptureSample(
        "face_id",
        EncodingUtilsImplForTests.base64ToBytes("template"),
        null,
        FACE_TEMPLATE_FORMAT
    )

    private val fingerprintCaptureResponse = FingerprintCaptureResponse(
        captureResult = listOf(
            FingerprintCaptureResult(
                Finger.LEFT_THUMB,
                fingerprintSample
            )
        )
    )

    private val faceCaptureResponse = FaceCaptureResponse(
        capturingResult = listOf(
            FaceCaptureResult(
                0,
                faceSample
            )
        )
    )

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var personCreationEventHelper: PersonCreationEventHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()
        coEvery { timeHelper.now() } returns CREATED_AT


        personCreationEventHelper =
            PersonCreationEventHelperImpl(eventRepository, timeHelper, EncodingUtilsImplForTests)
    }

    @Test
    fun addPersonCreationEventIfNeeded_shouldLoadEventsForCurrentSessions() {
        runBlocking {
            coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()

            personCreationEventHelper.addPersonCreationEventIfNeeded(emptyList())

            coVerify(atLeast = 2) { eventRepository.observeEventsFromSession(currentSession.id) }
        }
    }

    @Test
    fun fingerprintsCapturing_personCreationEventShouldHaveFingerprintsFieldsSet() {
        runBlocking {
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                currentSession,
                fingerprintCaptureBiometricsEvent
            )

            personCreationEventHelper.addPersonCreationEventIfNeeded(
                listOf(
                    fingerprintCaptureResponse
                )
            )

            coVerify {
                eventRepository.addOrUpdateEvent(any())
            }
        }
    }

    @Test
    fun facesCapturing_personCreationEventShouldHaveFacesFieldsSet() {
        runBlocking {
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                currentSession,
                faceCaptureBiometricsEvent
            )

            personCreationEventHelper.addPersonCreationEventIfNeeded(listOf(faceCaptureResponse))

            coVerify {
                eventRepository.addOrUpdateEvent(any())
            }
        }
    }

    @Test
    fun facesAndFingerprintsCapturing_personCreationEventShouldHaveFacesAndFingerprintsFieldsSet() {
        runBlocking {
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                currentSession,
                fingerprintCaptureBiometricsEvent
            )

            personCreationEventHelper.addPersonCreationEventIfNeeded(
                listOf(
                    fingerprintCaptureResponse,
                    faceCaptureResponse
                )
            )

            coVerify {
                eventRepository.addOrUpdateEvent(any())
            }
        }
    }

    @Test
    fun personCreationEventAlreadyExistsInCurrentSession_nothingHappens() {
        runBlocking {
            coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
                currentSession,
                fingerprintCaptureBiometricsEvent,
                faceCaptureBiometricsEvent,
                    createPersonCreationEvent()
            )

            personCreationEventHelper.addPersonCreationEventIfNeeded(
                listOf(
                    fingerprintCaptureResponse,
                    faceCaptureResponse
                )
            )

            coVerify(exactly = 0) {
                eventRepository.addOrUpdateEvent(any())
            }
        }
    }
}
