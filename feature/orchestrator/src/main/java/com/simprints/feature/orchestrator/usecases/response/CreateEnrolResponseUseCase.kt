package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import java.util.UUID
import javax.inject.Inject

internal class CreateEnrolResponseUseCase @Inject constructor(
    private val subjectFactory: SubjectFactory,
    private val enrolSubject: EnrolSubjectUseCase,
) {
    suspend operator fun invoke(
        request: ActionRequest.EnrolActionRequest,
        results: List<Serializable>,
        project: Project,
        enrolmentSubjectId: String,
    ): AppResponse {
        val fingerprintCapture = results.filterIsInstance(FingerprintCaptureResult::class.java).lastOrNull()
        val faceCapture = results.filterIsInstance(FaceCaptureResult::class.java).lastOrNull()
        val credentialResult = results.filterIsInstance(ExternalCredentialSearchResult::class.java).lastOrNull()
        val externalCredential = credentialResult?.scannedCredential?.toExternalCredential(enrolmentSubjectId)

        return try {
            val subject = subjectFactory.buildSubjectFromCaptureResults(
                subjectId = enrolmentSubjectId,
                projectId = request.projectId,
                attendantId = request.userId,
                moduleId = request.moduleId,
                fingerprintResponse = fingerprintCapture,
                faceResponse = faceCapture,
                externalCredential = externalCredential,
            )
            enrolSubject(subject, project)

            AppEnrolResponse(subject.subjectId)
        } catch (e: Exception) {
            Simber.e("Error creating enrol response", e, tag = ORCHESTRATION)
            AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
        }
    }

    private fun ScannedCredential.toExternalCredential(subjectId: String): ExternalCredential? {
        return ExternalCredential(
            id = UUID.randomUUID().toString(),
            value = credential,
            subjectId = subjectId,
            type = credentialType
        )
    }
}
