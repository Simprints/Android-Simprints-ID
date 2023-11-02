package com.simprints.matcher

import com.simprints.core.domain.common.FlowProvider
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.matcher.screen.MatchFragmentArgs

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
