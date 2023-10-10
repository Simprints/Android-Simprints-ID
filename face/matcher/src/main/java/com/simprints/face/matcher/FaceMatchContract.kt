package com.simprints.face.matcher

import com.simprints.core.domain.common.FlowProvider
import com.simprints.face.matcher.screen.FaceMatchFragmentArgs
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery

object FaceMatchContract {

    val DESTINATION = R.id.faceMatcherFragment
    val RESULT_CLASS = FaceMatchResult::class.java

    const val RESULT = "face_match_result"

    fun getArgs(
        faceSamples: List<FaceMatchParams.Sample>,
        flowType: FlowProvider.FlowType,
        subjectQuery: SubjectQuery,
    ) = FaceMatchFragmentArgs(FaceMatchParams(
        faceSamples,
        flowType,
        subjectQuery
    )).toBundle()
}
