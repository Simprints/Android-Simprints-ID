package com.simprints.feature.fetchsubject

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("FetchSubjectResult")
data class FetchSubjectResult(
    val found: Boolean,
    val wasOnline: Boolean = false,
) : StepResult
