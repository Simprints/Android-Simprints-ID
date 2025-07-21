package com.simprints.feature.orchestrator.steps

import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams

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
    val fingerprintSDK: FingerprintConfiguration.BioSdk?,
    val faceSDK: FaceConfiguration.BioSdk?,
) : StepParams {
    fun toFaceStepArgs(
        referenceId: String,
        samples: List<MatchParams.FaceSample>,
    ) = MatchContract.getParams(
        referenceId = referenceId,
        faceSamples = samples,
        faceSDK = faceSDK,
        flowType = flowType,
        subjectQuery = subjectQuery,
        biometricDataSource = biometricDataSource,
    )

    fun toFingerprintStepArgs(
        referenceId: String,
        samples: List<MatchParams.FingerprintSample>,
    ) = MatchContract.getParams(
        referenceId = referenceId,
        fingerprintSamples = samples,
        fingerprintSDK = fingerprintSDK,
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
            fingerprintSDK: FingerprintConfiguration.BioSdk? = null,
            faceSDK: FaceConfiguration.BioSdk? = null,
        ) = MatchStepStubPayload(flowType, subjectQuery, biometricDataSource, fingerprintSDK, faceSDK)
    }
}
