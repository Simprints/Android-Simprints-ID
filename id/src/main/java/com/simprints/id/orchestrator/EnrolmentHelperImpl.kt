package com.simprints.id.orchestrator

import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.EnrolmentEvent
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.tools.TimeHelper
import java.util.*

class EnrolmentHelperImpl(private val repository: SubjectRepository,
                          private val sessionRepository: SessionRepository,
                          private val timeHelper: TimeHelper) : EnrolmentHelper {

    override suspend fun enrol(subject: Subject) {
        saveAndUpload(subject)
        registerEvent(subject)
    }

    private suspend fun saveAndUpload(subject: Subject) {
        repository.saveAndUpload(subject)
    }

    private fun registerEvent(subject: Subject) {
        with(sessionRepository) {
            addEventToCurrentSessionInBackground(EnrolmentEvent(
                timeHelper.now(),
                subject.subjectId
            ))
        }
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
