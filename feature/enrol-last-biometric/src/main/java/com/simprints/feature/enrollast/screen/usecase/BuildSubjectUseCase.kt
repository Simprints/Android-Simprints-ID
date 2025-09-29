package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.Sample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import java.util.Date
import java.util.UUID
import javax.inject.Inject

internal class BuildSubjectUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val subjectFactory: SubjectFactory,
) {
    operator fun invoke(params: EnrolLastBiometricParams): Subject {
        val captureResult = params.steps
            .filterIsInstance<EnrolLastBiometricStepResult.CaptureResult>()
            .flatMap { result -> result.results.map { toSample(result.referenceId, it) } }

        return subjectFactory.buildSubject(
            UUID.randomUUID().toString(),
            params.projectId,
            params.userId,
            params.moduleId,
            createdAt = Date(timeHelper.now().ms),
            samples = captureResult,
            externalCredentials = getExternalCredentialResult(params.steps)?.let { listOf(it) } ?: emptyList(),
        )
    }

    // TODO [CORE-3421] When an external credential can be extracted from the UI-level steps, extract it here
    private fun getExternalCredentialResult(steps: List<EnrolLastBiometricStepResult>): ExternalCredential? = null

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
