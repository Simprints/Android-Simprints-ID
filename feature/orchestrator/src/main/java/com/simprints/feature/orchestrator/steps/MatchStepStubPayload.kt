package com.simprints.feature.orchestrator.steps

import android.os.Parcelable
import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.matcher.FaceMatchContract
import com.simprints.face.matcher.FaceMatchParams
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import kotlinx.parcelize.Parcelize

/**
 * Actual matching step payload is based on capture step results, so until the it is done we are storing
 * matching meta data in an intermediate payload that should be replaced once the capture step is successful.
 *
 * This also means that capture step MUST always strictly precede matching step.
 */
@Parcelize
internal data class MatchStepStubPayload(
    val flowType: FlowProvider.FlowType,
    val subjectQuery: SubjectQuery,
) : Parcelable {

    fun toFaceStepArgs(samples: List<FaceMatchParams.Sample>) = FaceMatchContract.getArgs(
        samples,
        flowType,
        subjectQuery,
    )

    // TODO toFingerprintStepArgs()

    companion object {
        const val STUB_KEY = "match_step_stub_payload"
    }
}
