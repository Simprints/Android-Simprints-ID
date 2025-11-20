package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Sample
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
            .flatMap { result -> result.results.map { toSample(result.referenceId, it) } }

        return subjectFactory.buildSubject(
            subjectId = subjectId,
            projectId = params.projectId,
            attendantId = params.userId,
            moduleId = params.moduleId,
            createdAt = Date(timeHelper.now().ms),
            samples = captureResult,
            externalCredentials = externalCredentials,
        )
    }

    private fun getExternalCredentialResult(
        credential: ScannedCredential?,
        subjectId: String,
    ) = credential?.toExternalCredential(subjectId)

    private fun toSample(
        referenceId: String,
        result: CaptureSample,
    ) = Sample(
        identifier = result.identifier,
        template = result.template,
        format = result.format,
        referenceId = referenceId,
        modality = result.modality,
    )
}
