package com.simprints.feature.fetchsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams

@Keep
data class FetchSubjectParams(
    val projectId: String,
    val subjectId: String,
    val metadata: String,
) : StepParams
