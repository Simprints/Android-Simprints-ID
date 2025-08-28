package com.simprints.matcher

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.config.store.models.ModalitySdkType

@Keep
data class MatchResult(
    val results: List<MatchResultItem>,
    val modality: Modality,
    val bioSdk: ModalitySdkType,
) : StepResult

@Keep
data class MatchResultItem(
    val subjectId: String,
    val confidence: Float,
) : StepResult
