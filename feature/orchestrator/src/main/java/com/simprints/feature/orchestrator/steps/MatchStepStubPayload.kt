package com.simprints.feature.orchestrator.steps

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.matching.MatchParams
import com.simprints.matcher.MatchContract

/**
 * Actual matching step payload is based on capture step results, so until the it is done we are storing
 * matching meta data in an intermediate payload that should be replaced once the capture step is successful.
 *
 * This also means that capture step MUST always strictly precede matching step.
 */
internal data class MatchStepStubPayload(
    val flowType: FlowType,
    val subjectQuery: SubjectQuery,
    val biometricDataSource: BiometricDataSource,
    val bioSdk: ModalitySdkType,
) : StepParams {
    fun toFaceStepArgs(
        referenceId: String,
        samples: List<CaptureSample>,
    ) = MatchContract.getParams(
        referenceId = referenceId,
        faceSamples = samples,
        bioSdk = bioSdk,
        flowType = flowType,
        subjectQuery = subjectQuery,
        biometricDataSource = biometricDataSource,
    )

    fun toFingerprintStepArgs(
        referenceId: String,
        samples: List<CaptureSample>,
    ) = MatchContract.getParams(
        referenceId = referenceId,
        fingerprintSamples = samples,
        bioSdk = bioSdk,
        flowType = flowType,
        subjectQuery = subjectQuery,
        biometricDataSource = biometricDataSource,
    )

    companion object {
        const val STUB_KEY = "match_step_stub_payload"

        fun getMatchStubParams(
            flowType: FlowType,
            subjectQuery: SubjectQuery,
            biometricDataSource: BiometricDataSource,
            bioSdk: ModalitySdkType,
        ) = MatchStepStubPayload(flowType, subjectQuery, biometricDataSource, bioSdk)
    }
}
