package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.eventsync.sync.common.EnrolmentRecordFactory
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class CreateEnrolResponseUseCase @Inject constructor(
    private val enrolmentRecordFactory: EnrolmentRecordFactory,
    private val enrolRecord: EnrolRecordUseCase,
) {
    suspend operator fun invoke(
        request: ActionRequest.EnrolActionRequest,
        results: List<Serializable>,
        project: Project,
        enrolmentSubjectId: String,
    ): AppResponse {
        val credentialResult = results.filterIsInstance<ExternalCredentialSearchResult>().lastOrNull()
        val externalCredential = credentialResult?.scannedCredential?.toExternalCredential(enrolmentSubjectId)

        return try {
            val record = enrolmentRecordFactory.buildFromCaptureResults(
                subjectId = enrolmentSubjectId,
                projectId = request.projectId,
                attendantId = request.userId,
                moduleId = request.moduleId,
                captures = results.filterIsInstance<BiometricReferenceCapture>(),
                externalCredential = externalCredential,
            )
            enrolRecord(record, project)

            AppEnrolResponse(record.subjectId, externalCredential)
        } catch (e: Exception) {
            Simber.e("Error creating enrol response", e, tag = ORCHESTRATION)
            AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
        }
    }
}
