package com.simprints.id.orchestrator.responsebuilders

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppEnrolResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintEnrolResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.TimeHelperImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class AppResponseBuilderForEnrol : AppResponseBuilder, BaseAppResponseBuilder() {

    @Inject lateinit var personRepository: PersonRepository

    override fun buildAppResponse(modalities: List<Modality>,
                                  appRequest: AppRequest,
                                  steps: List<Step>,
                                  sessionId: String): AppResponse {
        super.getErrorOrRefusalResponseIfAny(steps)?.let {
            return it
        }

        val request = appRequest as AppEnrolRequest
        val results = steps.map { it.result }
        val faceResponse = getFaceCaptureResponse(results)
        val fingerprintResponse = getFingerprintCaptureResponse(results)

        val person = buildPerson(request, fingerprintResponse, faceResponse)
        saveAndUpload(person)

        return buildAppEnrolResponse(person)
    }

    private fun getFaceCaptureResponse(results: List<Step.Result?>): FaceCaptureResponse? =
        results.filterIsInstance(FaceCaptureResponse::class.java).lastOrNull()

    private fun getFingerprintCaptureResponse(results: List<Step.Result?>): FingerprintEnrolResponse? =
        results.filterIsInstance(FingerprintEnrolResponse::class.java).lastOrNull()

    private fun buildPerson(request: AppEnrolRequest,
                            fingerprintResponse: FingerprintEnrolResponse?,
                            faceResponse: FaceCaptureResponse?): Person? {
        val isFingerprintAndFace = fingerprintResponse != null && faceResponse != null
        val isFingerprintOnly = fingerprintResponse != null
        val isFaceOnly = faceResponse != null

        return when {
            isFingerprintAndFace -> buildPersonFromFingerprintAndFace(request, fingerprintResponse!!, faceResponse!!)
            isFingerprintOnly -> buildPersonFromFingerprint(request, fingerprintResponse!!)
            isFaceOnly -> buildPersonFromFace(request, faceResponse!!)
            else -> null
        }
    }

    private fun saveAndUpload(person: Person?) {
        CoroutineScope(Dispatchers.Default).launch {
            if (person != null)
                personRepository.saveAndUpload(person)
        }
    }

    private fun buildPersonFromFingerprintAndFace(request: AppEnrolRequest,
                                                  fingerprintResponse: FingerprintEnrolResponse,
                                                  faceResponse: FaceCaptureResponse): Person {
        return Person(
            fingerprintResponse.guid,
            request.projectId,
            request.userId,
            request.moduleId,
            createdAt = Date(TimeHelperImpl().now()),
            fingerprintSamples = extractFingerprintSamples(fingerprintResponse),
            faceSamples = extractFaceSamples(faceResponse)
        )
    }

    private fun buildPersonFromFingerprint(request: AppEnrolRequest,
                                           fingerprintResponse: FingerprintEnrolResponse): Person {
        return Person(
            fingerprintResponse.guid,
            request.projectId,
            request.userId,
            request.moduleId,
            createdAt = Date(TimeHelperImpl().now())
        )
    }

    private fun buildPersonFromFace(request: AppEnrolRequest,
                                    faceResponse: FaceCaptureResponse): Person {
        val patientId = faceResponse.capturingResult.last().result?.faceId ?: throw Throwable("Patient ID is null")
        return Person(
            patientId,
            request.projectId,
            request.userId,
            request.moduleId,
            createdAt = Date(TimeHelperImpl().now())
        )
    }

    private fun extractFingerprintSamples(fingerprintResponse: FingerprintEnrolResponse): List<FingerprintSample> {
        TODO("awaiting implementation on fingerprint side")
    }

    private fun extractFaceSamples(faceResponse: FaceCaptureResponse): List<FaceSample> {
        return faceResponse.capturingResult.map {
            it.result?.toDomain() ?: throw Throwable("Face samples are null")
        }
    }

    private fun buildAppEnrolResponse(person: Person?): AppEnrolResponse {
        if (person == null)
            throw Throwable("App responses are null")
        return AppEnrolResponse(person.patientId)
    }

}
