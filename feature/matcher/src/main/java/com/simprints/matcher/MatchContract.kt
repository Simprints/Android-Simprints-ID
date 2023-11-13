package com.simprints.matcher

import com.simprints.core.domain.common.FlowType
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.matcher.screen.MatchFragmentArgs

object MatchContract {

    val DESTINATION = R.id.matcherFragment

    fun getArgs(
      fingerprintSamples: List<MatchParams.FingerprintSample> = emptyList(),
      faceSamples: List<MatchParams.FaceSample> = emptyList(),
      flowType: FlowType,
      subjectQuery: SubjectQuery,
    ) = MatchFragmentArgs(MatchParams(
        faceSamples,
        fingerprintSamples,
        flowType,
        subjectQuery
    )).toBundle()
}
