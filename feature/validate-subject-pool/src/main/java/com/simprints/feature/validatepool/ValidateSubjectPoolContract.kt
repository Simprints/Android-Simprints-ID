package com.simprints.feature.validatepool

import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

object ValidateSubjectPoolContract {
    val DESTINATION = R.id.validateSubjectPoolFragment

    fun getParams(subjectQuery: SubjectQuery) = ValidateSubjectPoolFragmentParams(subjectQuery)
}
