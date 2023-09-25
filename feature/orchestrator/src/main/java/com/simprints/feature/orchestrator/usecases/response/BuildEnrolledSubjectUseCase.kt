package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.infra.enrolment.records.domain.models.Subject
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class BuildEnrolledSubjectUseCase @Inject constructor(
    private val timeHelper: TimeHelper
) {

    operator fun invoke(
        projectId: String,
        userId: String,
        moduleId: String,
        // TODO add fingerprint results as well
        faceResponse: FaceCaptureResult?,
    ): Subject =
        when {
            // TODO
            //  fingerprintResponse != null && faceResponse != null -> {
            //      buildSubjectFromFingerprintAndFace(
            //          projectId,
            //          userId,
            //          moduleId,
            //          fingerprintResponse,
            //          faceResponse,
            //      )
            //  }
            //   fingerprintResponse != null -> {
            //       buildSubjectFromFingerprint(
            //           projectId,
            //           userId,
            //           moduleId,
            //           fingerprintResponse,
            //       )
            //   }

            faceResponse != null -> buildSubjectFromFace(projectId, userId, moduleId, faceResponse, timeHelper)

            else -> throw IllegalStateException("Missing capture results")
        }


    private fun buildSubjectFromFace(
        projectId: String,
        userId: String,
        moduleId: String,
        faceResponse: FaceCaptureResult,
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

    private fun extractFaceSamples(faceResponse: FaceCaptureResult) = faceResponse.results
        .mapNotNull { it.sample }
        .map { FaceSample(it.template, it.format) }


    private fun buildSubjectFromFingerprintAndFace(
        projectId: String,
        userId: String,
        moduleId: String,
        // TODO fingerprintResponse: FingerprintCaptureResponse,
        faceResponse: FaceCaptureResult,
    ): Subject {
        val patientId = UUID.randomUUID().toString()
        return Subject(
            patientId,
            projectId,
            userId,
            moduleId,
            createdAt = Date(timeHelper.now()),
            // TODO fingerprintSamples = extractFingerprintSamples(fingerprintResponse),
            faceSamples = extractFaceSamples(faceResponse)
        )
    }


    // TODO
    //    private fun buildSubjectFromFingerprint(
    //        projectId: String,
    //        userId: String,
    //        moduleId: String,
    //        fingerprintResponse: FingerprintCaptureResponse,
    //        timeHelper: TimeHelper
    //    ): Subject {
    //        val patientId = UUID.randomUUID().toString()
    //        return Subject(
    //            patientId,
    //            projectId,
    //            userId,
    //            moduleId,
    //            createdAt = Date(timeHelper.now()),
    //            fingerprintSamples = extractFingerprintSamples(fingerprintResponse)
    //        )
    //    }

    // TODO
    //    private fun extractFingerprintSamples(
    //        fingerprintResponse: FingerprintCaptureResponse
    //    ): List<FingerprintSample> {
    //        return fingerprintResponse.captureResult.mapNotNull { captureResult ->
    //            val fingerId = captureResult.identifier
    //            captureResult.sample?.let { sample ->
    //                FingerprintSample(
    //                    fingerId.fromDomainToModuleApi(),
    //                    sample.template,
    //                    sample.templateQualityScore,
    //                    sample.format
    //                )
    //            }
    //        }
    //    }

}
