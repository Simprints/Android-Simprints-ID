package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class MatchEntry(
    val candidateId: String,
    val score: Float,
)
