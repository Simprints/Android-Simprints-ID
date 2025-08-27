package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.infra.config.store.models.Finger
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
    )

    private fun getFingerprintCaptureResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.FingerprintCaptureResult>()
        .firstOrNull()

    private fun getFaceCaptureResult(steps: List<EnrolLastBiometricStepResult>) = steps
        .filterIsInstance<EnrolLastBiometricStepResult.FaceCaptureResult>()
        .firstOrNull()

    private fun fingerprintSample(
        referenceId: String,
        result: FingerTemplateCaptureResult,
    ) = Sample(
        identifier = fromDomainToModuleApi(result.finger),
        template = result.template,
        format = result.format,
        referenceId = referenceId,
        modality = Modality.FINGERPRINT,
    )

    private fun fromDomainToModuleApi(finger: Finger) = when (finger) {
        Finger.RIGHT_5TH_FINGER -> SampleIdentifier.RIGHT_5TH_FINGER
        Finger.RIGHT_4TH_FINGER -> SampleIdentifier.RIGHT_4TH_FINGER
        Finger.RIGHT_3RD_FINGER -> SampleIdentifier.RIGHT_3RD_FINGER
        Finger.RIGHT_INDEX_FINGER -> SampleIdentifier.RIGHT_INDEX_FINGER
        Finger.RIGHT_THUMB -> SampleIdentifier.RIGHT_THUMB
        Finger.LEFT_THUMB -> SampleIdentifier.LEFT_THUMB
        Finger.LEFT_INDEX_FINGER -> SampleIdentifier.LEFT_INDEX_FINGER
        Finger.LEFT_3RD_FINGER -> SampleIdentifier.LEFT_3RD_FINGER
        Finger.LEFT_4TH_FINGER -> SampleIdentifier.LEFT_4TH_FINGER
        Finger.LEFT_5TH_FINGER -> SampleIdentifier.LEFT_5TH_FINGER
    }

    private fun faceSample(
        referenceId: String,
        result: FaceTemplateCaptureResult,
    ) = Sample(
        template = result.template,
        format = result.format,
        referenceId = referenceId,
        modality = Modality.FACE,
    )
}
