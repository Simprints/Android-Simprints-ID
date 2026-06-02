package com.simprints.feature.validatepool

import com.simprints.feature.validatepool.ValidateSubjectPoolFragmentParams.ValidationMode
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery

object ValidateSubjectPoolContract {
    val DESTINATION = R.id.validateSubjectPoolFragment

    fun getParams(
        enrolmentRecordQuery: EnrolmentRecordQuery,
        mode: ValidationMode = ValidationMode.IDENTIFICATION,
    ) = ValidateSubjectPoolFragmentParams(enrolmentRecordQuery, mode)
}
