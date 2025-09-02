package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
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
        fingerprintSamples = getFingerprintCaptureResult(params.steps)
            ?.let { result -> result.results.map { fingerprintSample(result.referenceId, it) } }
            .orEmpty(),
        faceSamples = getFaceCaptureResult(params.steps)
            ?.let { result -> result.results.map { faceSample(result.referenceId, it) } }
            .orEmpty(),
        externalCredentials = getExternalCredentialResult(params.steps)?.let { listOf(it) } ?: emptyList()
    )

    // TODO [CORE-3421] When an external credential can be extracted from the UI-level steps, extract it here
    private fun getExternalCredentialResult(steps: List<EnrolLastBiometricStepResult>): ExternalCredential? {
        return null
    }

    private fun getFingerprintCaptureResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.FingerprintCaptureResult>()
        .firstOrNull()

    private fun getFaceCaptureResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.FaceCaptureResult>()
        .firstOrNull()

    private fun fingerprintSample(
        referenceId: String,
        result: FingerTemplateCaptureResult,
    ) = FingerprintSample(
        result.finger,
        result.template,
        result.format,
        referenceId,
    )

    private fun faceSample(
        referenceId: String,
        result: FaceTemplateCaptureResult,
    ) = FaceSample(result.template, result.format, referenceId)
}
