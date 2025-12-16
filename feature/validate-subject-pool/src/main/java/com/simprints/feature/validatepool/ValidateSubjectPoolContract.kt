package com.simprints.feature.validatepool

import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery

object ValidateSubjectPoolContract {
    val DESTINATION = R.id.validateSubjectPoolFragment

    fun getParams(enrolmentRecordQuery: EnrolmentRecordQuery) = ValidateSubjectPoolFragmentParams(enrolmentRecordQuery)
}
