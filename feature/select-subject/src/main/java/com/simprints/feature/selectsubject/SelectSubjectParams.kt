package com.simprints.feature.selectsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams

@Keep
data class SelectSubjectParams(
    val projectId: String,
    val subjectId: String,
) : StepParams
