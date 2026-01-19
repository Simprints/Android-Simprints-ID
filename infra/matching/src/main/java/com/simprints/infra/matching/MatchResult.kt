package com.simprints.infra.matching

import androidx.annotation.Keep
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName("MatchResult")
data class MatchResult(
    val results: List<ComparisonResult>,
    val sdk: ModalitySdkType,
) : StepResult
