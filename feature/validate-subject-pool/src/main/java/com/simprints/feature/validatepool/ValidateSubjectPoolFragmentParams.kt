package com.simprints.feature.validatepool

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery

@Keep
data class ValidateSubjectPoolFragmentParams(
    val enrolmentRecordQuery: EnrolmentRecordQuery,
) : StepParams
