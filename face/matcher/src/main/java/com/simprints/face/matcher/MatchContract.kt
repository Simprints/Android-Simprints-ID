package com.simprints.face.matcher

import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.matcher.screen.MatchFragmentArgs
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery

object MatchContract {

    val DESTINATION = R.id.matcherFragment

    const val RESULT = "match_result"

    fun getArgs(
        fingerprintSamples: List<MatchParams.FingerprintSample> = emptyList(),
        faceSamples: List<MatchParams.FaceSample> = emptyList(),
        flowType: FlowProvider.FlowType,
        subjectQuery: SubjectQuery,
    ) = MatchFragmentArgs(MatchParams(
        faceSamples,
        fingerprintSamples,
        flowType,
        subjectQuery
    )).toBundle()
}
