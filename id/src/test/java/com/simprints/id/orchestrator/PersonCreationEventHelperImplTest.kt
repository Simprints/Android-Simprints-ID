package com.simprints.id.orchestrator

import com.simprints.id.commontesttools.events.*
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerIdentifier.LEFT_THUMB
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.uniqueId
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureResult
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceCaptureSample
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintCaptureSample
import com.simprints.id.tools.EncodingUtilsTest
import com.simprints.id.tools.time.TimeHelper
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

    private val fingerprintCaptureEvent =
        createFingerprintCaptureEvent().let { it.copy(labels = it.labels.copy(sessionId = currentSession.id)) }

    private val faceCaptureEvent =
        createFaceCaptureEvent().let { it.copy(labels = it.labels.copy(sessionId = currentSession.id)) }

    private val fingerprintSample = FingerprintCaptureSample(
        LEFT_THUMB,
        templateQualityScore = 10,
        template = EncodingUtilsTest().base64ToBytes(
            fingerprintCaptureEvent.payload.fingerprint?.template
                ?: ""
        ),
        format = FingerprintTemplateFormat.ISO_19794_2
    )

    private val faceSample = FaceCaptureSample(
        "face_id",
        EncodingUtilsTest().base64ToBytes(faceCaptureEvent.payload.face?.template ?: ""),
        null,
        FaceTemplateFormat.RANK_ONE_1_23
    )

    private val fingerprintCaptureResponse = FingerprintCaptureResponse(
        captureResult = listOf(
            FingerprintCaptureResult(
                LEFT_THUMB,
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

    lateinit var personCreationEventHelper: PersonCreationEventHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createSessionCaptureEvent()
        coEvery { eventRepository.loadEvents(any()) } returns emptyFlow()
        coEvery { timeHelper.now() } returns CREATED_AT


        personCreationEventHelper = PersonCreationEventHelperImpl(eventRepository, timeHelper, EncodingUtilsTest())
    }

    @Test
    fun addPersonCreationEventIfNeeded_shouldLoadEventsForCurrentSessions() {
        runBlocking {
            coEvery { eventRepository.loadEvents(any()) } returns emptyFlow()

            personCreationEventHelper.addPersonCreationEventIfNeeded(emptyList())

            coVerify(atLeast = 2) { eventRepository.loadEvents(currentSession.id) }
        }
    }

    @Test
    fun fingerprintsCapturing_personCreationEventShouldHaveFingerprintsFieldsSet() {
        runBlocking {
            coEvery { eventRepository.loadEvents(any()) } returns flowOf(currentSession, fingerprintCaptureEvent)

            personCreationEventHelper.addPersonCreationEventIfNeeded(listOf(fingerprintCaptureResponse))

            coVerify {
                eventRepository.addEventToCurrentSession(match {
                    it == PersonCreationEvent(
                        startTime = CREATED_AT,
                        fingerprintCaptureIds = listOf(fingerprintCaptureEvent.id),
                        fingerprintReferenceId = listOf(
                            FingerprintSample(
                                fingerprintSample.fingerIdentifier,
                                fingerprintSample.template,
                                fingerprintSample.templateQualityScore,
                                fingerprintSample.format
                            )
                        ).uniqueId(),
                        faceCaptureIds = null,
                        faceReferenceId = null
                    ).copy(id = it.id)
                })
            }
        }
    }

    @Test
    fun facesCapturing_personCreationEventShouldHaveFacesFieldsSet() {
        runBlocking {
            coEvery { eventRepository.loadEvents(any()) } returns flowOf(currentSession, faceCaptureEvent)

            personCreationEventHelper.addPersonCreationEventIfNeeded(listOf(faceCaptureResponse))

            coVerify {
                eventRepository.addEventToCurrentSession(match {
                    it == PersonCreationEvent(
                        startTime = CREATED_AT,
                        fingerprintCaptureIds = null,
                        fingerprintReferenceId = null,
                        faceCaptureIds = listOf(faceCaptureEvent.id),
                        faceReferenceId = listOf(FaceSample(faceSample.template, faceSample.format)).uniqueId()
                    ).copy(id = it.id)
                })
            }
        }
    }

    @Test
    fun facesAndFingerprintsCapturing_personCreationEventShouldHaveFacesAndFingerprintsFieldsSet() {
        runBlocking {
            coEvery { eventRepository.loadEvents(any()) } returns flowOf(
                currentSession,
                fingerprintCaptureEvent,
                faceCaptureEvent
            )

            personCreationEventHelper.addPersonCreationEventIfNeeded(
                listOf(
                    fingerprintCaptureResponse,
                    faceCaptureResponse
                )
            )

            coVerify {
                eventRepository.addEventToCurrentSession(match {
                    it == PersonCreationEvent(
                        startTime = CREATED_AT,
                        fingerprintCaptureIds = listOf(fingerprintCaptureEvent.id),
                        fingerprintReferenceId = listOf(
                            FingerprintSample(
                                fingerprintSample.fingerIdentifier,
                                fingerprintSample.template,
                                fingerprintSample.templateQualityScore,
                                fingerprintSample.format
                            )
                        ).uniqueId(),
                        faceCaptureIds = listOf(faceCaptureEvent.id),
                        faceReferenceId = listOf(FaceSample(faceSample.template, faceSample.format)).uniqueId()
                    ).copy(id = it.id)
                })
            }
        }
    }

    @Test
    fun personCreationEventAlreadyExistsInCurrentSession_nothingHappens() {
        runBlocking {
            coEvery { eventRepository.loadEvents(any()) } returns flowOf(
                currentSession,
                fingerprintCaptureEvent,
                faceCaptureEvent,
                createPersonCreationEvent()
            )

            personCreationEventHelper.addPersonCreationEventIfNeeded(
                listOf(
                    fingerprintCaptureResponse,
                    faceCaptureResponse
                )
            )

            coVerify(exactly = 0) {
                eventRepository.addEventToCurrentSession(any())
            }
        }
    }
}
