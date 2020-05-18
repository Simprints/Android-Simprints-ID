package com.simprints.id.orchestrator

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.EnrolmentEvent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.tools.TimeHelper
import java.util.*

class EnrolmentHelperImpl(private val repository: PersonRepository,
                          private val sessionRepository: SessionRepository,
                          private val timeHelper: TimeHelper) : EnrolmentHelper {

    override suspend fun enrol(person: Person) {
        saveAndUpload(person)
        registerEvent(person)
    }

    private suspend fun saveAndUpload(person: Person) {
        repository.saveAndUpload(person)
    }

    private fun registerEvent(person: Person) {
        with(sessionRepository) {
            addEventToCurrentSessionInBackground(EnrolmentEvent(
                timeHelper.now(),
                person.patientId
            ))
        }
    }

    override fun buildPerson(projectId: String,
                             userId: String,
                             moduleId: String,
                             fingerprintResponse: FingerprintCaptureResponse?,
                             faceResponse: FaceCaptureResponse?,
                             timeHelper: TimeHelper): Person =
        when {
            fingerprintResponse != null && faceResponse != null -> {
                buildPersonFromFingerprintAndFace(projectId, userId, moduleId, fingerprintResponse, faceResponse, timeHelper)
            }

            fingerprintResponse != null -> {
                buildPersonFromFingerprint(projectId, userId, moduleId, fingerprintResponse, timeHelper)
            }

            faceResponse != null -> {
                buildPersonFromFace(projectId, userId, moduleId, faceResponse, timeHelper)
            }

            else -> throw Throwable("Invalid response. Must be either fingerprint, face or both")
        }


    private fun buildPersonFromFingerprintAndFace(projectId: String,
                                                  userId: String,
                                                  moduleId: String,
                                                  fingerprintResponse: FingerprintCaptureResponse,
                                                  faceResponse: FaceCaptureResponse,
                                                  timeHelper: TimeHelper): Person {
        val patientId = UUID.randomUUID().toString()
        return Person(
            patientId,
            projectId,
            userId,
            moduleId,
            createdAt = Date(timeHelper.now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse),
            faceSamples = extractFaceSamples(faceResponse)
        )
    }

    private fun buildPersonFromFingerprint(projectId: String,
                                           userId: String,
                                           moduleId: String,
                                           fingerprintResponse: FingerprintCaptureResponse,
                                           timeHelper: TimeHelper): Person {
        val patientId = UUID.randomUUID().toString()
        return Person(
            patientId,
            projectId,
            userId,
            moduleId,
            createdAt = Date(timeHelper.now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse)
        )
    }

    private fun buildPersonFromFace(projectId: String,
                                    userId: String,
                                    moduleId: String,
                                    faceResponse: FaceCaptureResponse,
                                    timeHelper: TimeHelper): Person {
        val patientId = UUID.randomUUID().toString()
        return Person(
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
