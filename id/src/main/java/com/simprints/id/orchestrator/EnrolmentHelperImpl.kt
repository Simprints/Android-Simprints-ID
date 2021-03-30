package com.simprints.id.orchestrator

import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.EnrolmentEventV2
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.*

private const val TAG = "ENROLMENT"

class EnrolmentHelperImpl(
    private val subjectRepository: SubjectRepository,
    private val eventRepository: EventRepository,
    private val loginInfoManager: LoginInfoManager,
    private val timeHelper: TimeHelper
) : EnrolmentHelper {

    override suspend fun enrol(subject: Subject) {
        Timber.tag(TAG).d("Enrolment in progress")
        registerEvent(subject)

        Timber.tag(TAG).d("Create a subject record")
        subjectRepository.performActions(listOf(SubjectAction.Creation(subject)))

        Timber.tag(TAG).d("Up-syncing")
        inBackground {
            eventRepository.uploadEvents(projectId = loginInfoManager.getSignedInProjectIdOrEmpty()).collect()
        }

        Timber.tag(TAG).d("Done!")
    }

    private suspend fun registerEvent(subject: Subject) {
        Timber.tag(TAG).d("Register events for enrolments")

        val currentSession = eventRepository.getCurrentCaptureSessionEvent().id
        val personCreationEvent = eventRepository.getEventsFromSession(currentSession).filterIsInstance<PersonCreationEvent>().first()

        eventRepository.addEvent(
            EnrolmentEventV2(
                timeHelper.now(),
                subject.subjectId,
                subject.projectId,
                subject.moduleId,
                subject.attendantId,
                personCreationEvent.id
            )
        )
    }

    override fun buildSubject(
        projectId: String,
        userId: String,
        moduleId: String,
        fingerprintResponse: FingerprintCaptureResponse?,
        faceResponse: FaceCaptureResponse?,
        timeHelper: TimeHelper
    ): Subject =
        when {
            fingerprintResponse != null && faceResponse != null -> {
                buildSubjectFromFingerprintAndFace(
                    projectId,
                    userId,
                    moduleId,
                    fingerprintResponse,
                    faceResponse,
                    timeHelper
                )
            }

            fingerprintResponse != null -> {
                buildSubjectFromFingerprint(projectId, userId, moduleId, fingerprintResponse, timeHelper)
            }

            faceResponse != null -> {
                buildSubjectFromFace(projectId, userId, moduleId, faceResponse, timeHelper)
            }

            else -> throw Throwable("Invalid response. Must be either fingerprint, face or both")
        }


    private fun buildSubjectFromFingerprintAndFace(
        projectId: String,
        userId: String,
        moduleId: String,
        fingerprintResponse: FingerprintCaptureResponse,
        faceResponse: FaceCaptureResponse,
        timeHelper: TimeHelper
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return Subject(
            patientId,
            projectId,
            userId,
            moduleId,
            createdAt = Date(timeHelper.now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse),
            faceSamples = extractFaceSamples(faceResponse)
        )
    }

    private fun buildSubjectFromFingerprint(
        projectId: String,
        userId: String,
        moduleId: String,
        fingerprintResponse: FingerprintCaptureResponse,
        timeHelper: TimeHelper
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return Subject(
            patientId,
            projectId,
            userId,
            moduleId,
            createdAt = Date(timeHelper.now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse)
        )
    }

    private fun buildSubjectFromFace(
        projectId: String,
        userId: String,
        moduleId: String,
        faceResponse: FaceCaptureResponse,
        timeHelper: TimeHelper
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return Subject(
            patientId,
            projectId,
            userId,
            moduleId,
            createdAt = Date(timeHelper.now()),
            faceSamples = extractFaceSamples(faceResponse)
        )
    }

    private fun extractFingerprintSamples(
        fingerprintResponse: FingerprintCaptureResponse
    ): List<FingerprintSample> {
        return fingerprintResponse.captureResult.mapNotNull { captureResult ->
            val fingerId = captureResult.identifier
            captureResult.sample?.let { sample ->
                FingerprintSample(fingerId, sample.template, sample.templateQualityScore, sample.format)
            }
        }
    }

    private fun extractFaceSamples(faceResponse: FaceCaptureResponse) =
        faceResponse.capturingResult.mapNotNull { captureResult ->
            captureResult.result?.let { sample ->
                FaceSample(sample.template, sample.format)
            }
        }
}
