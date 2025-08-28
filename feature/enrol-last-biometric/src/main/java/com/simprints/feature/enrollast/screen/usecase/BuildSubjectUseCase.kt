package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.modality.Modality
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
    operator fun invoke(params: EnrolLastBiometricParams): Subject = subjectFactory.buildSubject(
        UUID.randomUUID().toString(),
        params.projectId,
        params.userId,
        params.moduleId,
        createdAt = Date(timeHelper.now().ms),
        fingerprintSamples = getCaptureResult(params.steps, Modality.FINGERPRINT)
            ?.let { result -> result.results.map { getSample(result.referenceId, it) } }
            .orEmpty(),
        faceSamples = getCaptureResult(params.steps, Modality.FACE)
            ?.let { result -> result.results.map { getSample(result.referenceId, it) } }
            .orEmpty(),
    )

    private fun getCaptureResult(
        steps: List<EnrolLastBiometricStepResult>,
        modality: Modality,
    ) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.CaptureResult>()
        .firstOrNull { it.modality == modality }

    private fun getSample(
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
