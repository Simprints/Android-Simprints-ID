package com.simprints.feature.orchestrator.usecases.response

import com.simprints.core.domain.capture.BiometricReferenceCapture
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
import com.simprints.infra.protection.TemplateProtection
import java.io.Serializable
import javax.inject.Inject

internal class CreateEnrolResponseUseCase @Inject constructor(
    private val enrolmentRecordFactory: EnrolmentRecordFactory,
    private val enrolRecord: EnrolRecordUseCase,
    private val templateProtection: TemplateProtection,
) {
    suspend operator fun invoke(
        request: ActionRequest.EnrolActionRequest,
        results: List<Serializable>,
        project: Project,
        enrolmentSubjectId: String,
    ): AppResponse {
        val credentialResult = results.filterIsInstance<ExternalCredentialSearchResult>().lastOrNull()
        val externalCredential = credentialResult?.scannedCredential?.toExternalCredential(enrolmentSubjectId)

        val auxData = templateProtection.createAuxData()
        return try {
            val record = enrolmentRecordFactory.buildFromCaptureResults(
                subjectId = enrolmentSubjectId,
                projectId = request.projectId,
                attendantId = request.userId,
                moduleId = request.moduleId,
                captures = results.filterIsInstance<BiometricReferenceCapture>().map { reference ->
                    // TODO PoC - overriding captured templates with encoded ones
                    reference.copy(
                        templates = reference.templates.map { templateCapture ->
                            templateCapture.copy(
                                template = templateProtection.encodeTemplate(
                                    template = templateCapture.template,
                                    auxData = auxData,
                                ),
                            )
                        },
                    )
                },
                externalCredential = externalCredential,
            )
            enrolRecord(record, project)

            // TODO PoC - Storing aux data for this subject
            templateProtection.saveAuxData(enrolmentSubjectId, auxData)

            AppEnrolResponse(record.subjectId, externalCredential)
        } catch (e: Exception) {
            Simber.e("Error creating enrol response", e, tag = ORCHESTRATION)
            AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)
        }
    }
}
