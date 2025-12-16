package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import java.util.Date
import java.util.UUID
import javax.inject.Inject

internal class BuildSubjectUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val subjectFactory: SubjectFactory,
) {
    operator fun invoke(
        params: EnrolLastBiometricParams,
        isAddingCredential: Boolean,
    ): Subject {
        val subjectId = UUID.randomUUID().toString()
        val externalCredentials = if (isAddingCredential) {
            getExternalCredentialResult(params.scannedCredential, subjectId)?.let(::listOf) ?: emptyList()
        } else {
            emptyList()
        }
        val captureResult = params.steps
            .filterIsInstance<EnrolLastBiometricStepResult.CaptureResult>()
            .map { result -> result.result.toBiometricReference() }

        return subjectFactory.buildSubject(
            subjectId = subjectId,
            projectId = params.projectId,
            attendantId = params.userId,
            moduleId = params.moduleId,
            createdAt = Date(timeHelper.now().ms),
            references = captureResult,
            externalCredentials = externalCredentials,
        )
    }

    private fun getExternalCredentialResult(
        credential: ScannedCredential?,
        subjectId: String,
    ) = credential?.toExternalCredential(subjectId)

    private fun BiometricReferenceCapture.toBiometricReference() = BiometricReference(
        referenceId = referenceId,
        modality = modality,
        format = format,
        templates = templates.map { templateCapture ->
            BiometricTemplate(
                identifier = templateCapture.identifier,
                template = templateCapture.template,
            )
        },
    )
}
