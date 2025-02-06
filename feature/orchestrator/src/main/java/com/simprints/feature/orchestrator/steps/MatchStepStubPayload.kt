package com.simprints.feature.orchestrator.steps

import android.os.Parcelable
import androidx.core.os.bundleOf
import com.simprints.core.domain.common.FlowType
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams
import kotlinx.parcelize.Parcelize

/**
 * Actual matching step payload is based on capture step results, so until the it is done we are storing
 * matching meta data in an intermediate payload that should be replaced once the capture step is successful.
 *
 * This also means that capture step MUST always strictly precede matching step.
 */
@Parcelize
internal data class MatchStepStubPayload(
    val flowType: FlowType,
    val subjectQuery: SubjectQuery,
    val biometricDataSource: BiometricDataSource,
    val fingerprintSDK: FingerprintConfiguration.BioSdk?,
) : Parcelable {
    fun toFaceStepArgs(
        referenceId: String,
        samples: List<MatchParams.FaceSample>,
    ) = MatchContract.getArgs(
        referenceId = referenceId,
        faceSamples = samples,
        flowType = flowType,
        subjectQuery = subjectQuery,
        biometricDataSource = biometricDataSource,
    )

    fun toFingerprintStepArgs(
        referenceId: String,
        samples: List<MatchParams.FingerprintSample>,
    ) = MatchContract.getArgs(
        referenceId = referenceId,
        fingerprintSamples = samples,
        fingerprintSDK = fingerprintSDK,
        flowType = flowType,
        subjectQuery = subjectQuery,
        biometricDataSource = biometricDataSource,
    )

    companion object {
        const val STUB_KEY = "match_step_stub_payload"

        fun asBundle(
            flowType: FlowType,
            subjectQuery: SubjectQuery,
            biometricDataSource: BiometricDataSource,
            fingerprintSDK: FingerprintConfiguration.BioSdk? = null,
        ) = bundleOf(STUB_KEY to MatchStepStubPayload(flowType, subjectQuery, biometricDataSource, fingerprintSDK))
    }
}
