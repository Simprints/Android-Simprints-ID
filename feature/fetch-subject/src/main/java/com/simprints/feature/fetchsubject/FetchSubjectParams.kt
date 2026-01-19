package com.simprints.feature.fetchsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("FetchSubjectParams")
data class FetchSubjectParams(
    val projectId: String,
    val subjectId: String,
    val metadata: String,
) : StepParams
