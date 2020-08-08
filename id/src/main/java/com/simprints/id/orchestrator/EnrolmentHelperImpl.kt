package com.simprints.id.orchestrator

import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.EnrolmentEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.Companion.buildBiometricReferences
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.tools.TimeHelper
import java.util.*

class EnrolmentHelperImpl(private val subjectRepository: SubjectRepository,
                          private val eventRepository: EventRepository,
                          private val eventSyncManager: EventSyncManager,
                          private val preferencesManager: PreferencesManager,
                          private val timeHelper: TimeHelper) : EnrolmentHelper {

    override suspend fun enrol(subject: Subject) {
        registerEvent(subject)
        subjectRepository.save(subject)
        eventSyncManager.sync()
    }

    private suspend fun registerEvent(subject: Subject) {
        eventRepository.addEventToCurrentSession(
            EnrolmentEvent(timeHelper.now(), subject.subjectId)
        )

        eventRepository.addEventToCurrentSession(
            EnrolmentRecordCreationEvent(
                timeHelper.now(),
                subject.subjectId,
                subject.projectId,
                subject.moduleId,
                subject.attendantId,
                preferencesManager.modalities.map { it.toMode() },
                buildBiometricReferences(subject.fingerprintSamples, subject.faceSamples)
            )
        )
    }

    override fun buildSubject(projectId: String,
                              userId: String,
                              moduleId: String,
                              fingerprintResponse: FingerprintCaptureResponse?,
                              faceResponse: FaceCaptureResponse?,
                              timeHelper: TimeHelper): Subject =
        when {
            fingerprintResponse != null && faceResponse != null -> {
                buildSubjectFromFingerprintAndFace(projectId, userId, moduleId, fingerprintResponse, faceResponse, timeHelper)
            }

            fingerprintResponse != null -> {
                buildSubjectFromFingerprint(projectId, userId, moduleId, fingerprintResponse, timeHelper)
            }

            faceResponse != null -> {
                buildSubjectFromFace(projectId, userId, moduleId, faceResponse, timeHelper)
            }

            else -> throw Throwable("Invalid response. Must be either fingerprint, face or both")
        }


    private fun buildSubjectFromFingerprintAndFace(projectId: String,
                                                   userId: String,
                                                   moduleId: String,
                                                   fingerprintResponse: FingerprintCaptureResponse,
                                                   faceResponse: FaceCaptureResponse,
                                                   timeHelper: TimeHelper): Subject {
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

    private fun buildSubjectFromFingerprint(projectId: String,
                                            userId: String,
                                            moduleId: String,
                                            fingerprintResponse: FingerprintCaptureResponse,
                                            timeHelper: TimeHelper): Subject {
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

    private fun buildSubjectFromFace(projectId: String,
                                     userId: String,
                                     moduleId: String,
                                     faceResponse: FaceCaptureResponse,
                                     timeHelper: TimeHelper): Subject {
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
                FingerprintSample(fingerId, sample.template, sample.templateQualityScore)
            }
        }
    }

    private fun extractFaceSamples(faceResponse: FaceCaptureResponse) =
        faceResponse.capturingResult.mapNotNull { it ->
            it.result?.let {
                FaceSample(it.template)
            }
        }
}
