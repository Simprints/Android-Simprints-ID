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
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.moduleapi.fingerprint.responses.IFingerprintCaptureResponse
import java.util.*

class AppResponseBuilderForEnrol(
    private val repository: PersonRepository
) : BaseAppResponseBuilder() {

    override suspend fun buildAppResponse(modalities: List<Modality>,
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

        val person = PersonBuilder.buildPerson(request, fingerprintResponse, faceResponse)
        EnrolmentHelper(repository).saveAndUpload(person)

        return AppEnrolResponse(person.patientId)
    }

    private fun getFaceCaptureResponse(results: List<Step.Result?>): FaceCaptureResponse? =
        results.filterIsInstance(FaceCaptureResponse::class.java).lastOrNull()

    private fun getFingerprintCaptureResponse(results: List<Step.Result?>): FingerprintEnrolResponse? =
        results.filterIsInstance(FingerprintEnrolResponse::class.java).lastOrNull()

    object PersonBuilder {
        fun buildPerson(request: AppEnrolRequest,
                        fingerprintResponse: FingerprintEnrolResponse?,
                        faceResponse: FaceCaptureResponse?): Person {
            return when {
                fingerprintResponse != null && faceResponse != null -> {
                    buildPersonFromFingerprintAndFace(request, fingerprintResponse, faceResponse)
                }

                fingerprintResponse != null -> {
                    buildPersonFromFingerprint(request, fingerprintResponse)
                }

                faceResponse != null -> {
                    buildPersonFromFace(request, faceResponse)
                }

                else -> throw Throwable("Invalid response. Must be either fingerprint, face or both")
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
                createdAt = Date(TimeHelperImpl().now()),
                fingerprintSamples = extractFingerprintSamples(fingerprintResponse)
            )
        }

        private fun buildPersonFromFace(request: AppEnrolRequest,
                                        faceResponse: FaceCaptureResponse): Person {
            val patientId = UUID.randomUUID().toString()
            return Person(
                patientId,
                request.projectId,
                request.userId,
                request.moduleId,
                createdAt = Date(TimeHelperImpl().now()),
                faceSamples = extractFaceSamples(faceResponse)
            )
        }

        private fun extractFingerprintSamples(fingerprintResponse: IFingerprintCaptureResponse): List<FingerprintSample> {
            // TODO: awaiting implementation on fingerprint side
            return emptyList()
        }

        private fun extractFaceSamples(faceResponse: FaceCaptureResponse): List<FaceSample> {
            return faceResponse.capturingResult.mapNotNull {
                val imageRef = it.result?.imageRef
                it.result?.template?.let { template ->
                    FaceSample(template, imageRef)
                }
            }
        }
    }

}
