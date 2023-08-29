package com.simprints.id.orchestrator

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.models.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.exceptions.unexpected.MissingCaptureResponse
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.domain.models.SubjectAction
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.UUID
import javax.inject.Inject

private const val TAG = "ENROLMENT"

class EnrolmentHelperImpl @Inject constructor(
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val eventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    private val subjectFactory: SubjectFactory
) : EnrolmentHelper {

    override suspend fun enrol(subject: Subject) {
        Simber.tag(TAG).d("Enrolment in progress")
        registerEvent(subject)

        Simber.tag(TAG).d("Create a subject record")
        enrolmentRecordManager.performActions(listOf(SubjectAction.Creation(subject)))

        Simber.tag(TAG).d("Done!")
    }

    private suspend fun registerEvent(subject: Subject) {
        Simber.tag(TAG).d("Register events for enrolments")

        val currentSession = eventRepository.getCurrentCaptureSessionEvent().id
        val personCreationEvent = eventRepository.observeEventsFromSession(currentSession)
            .filterIsInstance<PersonCreationEvent>().first()

        eventRepository.addOrUpdateEvent(
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

    override suspend fun buildSubject(
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
                buildSubjectFromFingerprint(
                    projectId,
                    userId,
                    moduleId,
                    fingerprintResponse,
                    timeHelper
                )
            }

            faceResponse != null -> {
                buildSubjectFromFace(projectId, userId, moduleId, faceResponse, timeHelper)
            }

            else -> throw MissingCaptureResponse()
        }


    private suspend fun buildSubjectFromFingerprintAndFace(
        projectId: String,
        userId: String,
        moduleId: String,
        fingerprintResponse: FingerprintCaptureResponse,
        faceResponse: FaceCaptureResponse,
        timeHelper: TimeHelper
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return subjectFactory.buildEncryptedSubject(
            subjectId = patientId,
            projectId = projectId,
            attendantId = userId,
            moduleId = moduleId,
            createdAt = Date(timeHelper.now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse),
            faceSamples = extractFaceSamples(faceResponse)
        )
    }

    private suspend fun buildSubjectFromFingerprint(
        projectId: String,
        userId: String,
        moduleId: String,
        fingerprintResponse: FingerprintCaptureResponse,
        timeHelper: TimeHelper
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return subjectFactory.buildEncryptedSubject(
            subjectId = patientId,
            projectId = projectId,
            attendantId = userId,
            moduleId = moduleId,
            createdAt = Date(timeHelper.now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse)
        )
    }

    private suspend fun buildSubjectFromFace(
        projectId: String,
        userId: String,
        moduleId: String,
        faceResponse: FaceCaptureResponse,
        timeHelper: TimeHelper
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return subjectFactory.buildEncryptedSubject(
            subjectId = patientId,
            projectId = projectId,
            attendantId = userId,
            moduleId = moduleId,
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
                FingerprintSample(
                    fingerId.fromDomainToModuleApi(),
                    sample.template,
                    sample.templateQualityScore,
                    sample.format
                )
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
