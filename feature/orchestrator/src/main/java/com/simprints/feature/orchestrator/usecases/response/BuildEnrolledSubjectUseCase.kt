package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.orchestrator.exceptions.MissingCaptureException
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import javax.inject.Inject

class BuildEnrolledSubjectUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val subjectFactory: SubjectFactory
) {

    operator fun invoke(
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
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

            faceResponse != null -> subjectFactory.buildSubjectFromFace(projectId, userId, moduleId, faceResponse, timeHelper)

            else -> throw MissingCaptureException()
        }


    // TODO
    //    private fun buildSubjectFromFingerprintAndFace(
    //        projectId: String,
    //        userId: String,
    //        moduleId: String,
    //        fingerprintResponse: FingerprintCaptureResponse,
    //        faceResponse: FaceCaptureResult,
    //    ): Subject {
    //        val patientId = UUID.randomUUID().toString()
    //        return Subject(
    //            patientId,
    //            projectId,
    //            userId,
    //            moduleId,
    //            createdAt = Date(timeHelper.now()),
    //            fingerprintSamples = extractFingerprintSamples(fingerprintResponse),
    //            faceSamples = extractFaceSamples(faceResponse)
    //        )
    //    }


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
