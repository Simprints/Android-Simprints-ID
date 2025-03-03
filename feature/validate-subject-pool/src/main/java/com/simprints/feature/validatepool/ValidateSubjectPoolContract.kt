package com.simprints.feature.validatepool

import android.os.Bundle
import com.simprints.feature.validatepool.screen.ValidateSubjectPoolFragmentArgs
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery

object ValidateSubjectPoolContract {
    fun getArgs(subjectQuery: SubjectQuery): Bundle = ValidateSubjectPoolFragmentArgs(subjectQuery).toBundle()

    val DESTINATION = R.id.validateSubjectPoolFragment
}
