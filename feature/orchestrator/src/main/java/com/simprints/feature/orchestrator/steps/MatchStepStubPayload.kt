package com.simprints.feature.orchestrator.steps

import android.os.Parcelable
import androidx.core.os.bundleOf
import com.simprints.core.domain.common.FlowType
import com.simprints.matcher.MatchContract
import com.simprints.matcher.MatchParams
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
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
) : Parcelable {

    fun toFaceStepArgs(samples: List<MatchParams.FaceSample>) = MatchContract.getArgs(
        faceSamples = samples,
        flowType = flowType,
        subjectQuery = subjectQuery,
    )

    fun toFingerprintStepArgs(samples: List<MatchParams.FingerprintSample>) = MatchContract.getArgs(
        fingerprintSamples = samples,
        flowType = flowType,
        subjectQuery =    subjectQuery,
    )

    companion object {
        const val STUB_KEY = "match_step_stub_payload"

        fun asBundle(
          flowType: FlowType,
          subjectQuery: SubjectQuery,
        ) = bundleOf(STUB_KEY to MatchStepStubPayload(flowType, subjectQuery))
    }
}
