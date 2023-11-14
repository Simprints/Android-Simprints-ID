package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import java.util.Date
import java.util.UUID
import javax.inject.Inject

internal class BuildSubjectUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val subjectFactory: SubjectFactory
) {

    operator fun invoke(params: EnrolLastBiometricParams): Subject = subjectFactory.buildSubject(
        UUID.randomUUID().toString(),
        params.projectId,
        params.userId,
        params.moduleId,
        createdAt = Date(timeHelper.now()),
        fingerprintSamples = getFingerprintCaptureResult(params.steps)?.map(::fingerprintSample)
            .orEmpty(),
        faceSamples = getFaceCaptureResult(params.steps)?.map(::faceSample).orEmpty()
    )

    private fun getFingerprintCaptureResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.FingerprintCaptureResult>()
            .firstOrNull()?.results

    private fun getFaceCaptureResult(steps: List<EnrolLastBiometricStepResult>) =
        steps.filterIsInstance<EnrolLastBiometricStepResult.FaceCaptureResult>()
            .firstOrNull()?.results

    private fun fingerprintSample(it: FingerTemplateCaptureResult) = FingerprintSample(
        fromDomainToModuleApi(it.finger),
        it.template,
        it.templateQualityScore,
        it.format
    )

    private fun fromDomainToModuleApi(finger: Finger) = when (finger) {
        Finger.RIGHT_5TH_FINGER -> IFingerIdentifier.RIGHT_5TH_FINGER
        Finger.RIGHT_4TH_FINGER -> IFingerIdentifier.RIGHT_4TH_FINGER
        Finger.RIGHT_3RD_FINGER -> IFingerIdentifier.RIGHT_3RD_FINGER
        Finger.RIGHT_INDEX_FINGER -> IFingerIdentifier.RIGHT_INDEX_FINGER
        Finger.RIGHT_THUMB -> IFingerIdentifier.RIGHT_THUMB
        Finger.LEFT_THUMB -> IFingerIdentifier.LEFT_THUMB
        Finger.LEFT_INDEX_FINGER -> IFingerIdentifier.LEFT_INDEX_FINGER
        Finger.LEFT_3RD_FINGER -> IFingerIdentifier.LEFT_3RD_FINGER
        Finger.LEFT_4TH_FINGER -> IFingerIdentifier.LEFT_4TH_FINGER
        Finger.LEFT_5TH_FINGER -> IFingerIdentifier.LEFT_5TH_FINGER
    }

    private fun faceSample(it: FaceTemplateCaptureResult) = FaceSample(it.template, it.format)

}
