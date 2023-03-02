package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.MatchEntry as MatchEntryCore

@Keep
data class MatchEntry(val candidateId: String, val score: Float) {
    fun fromDomainToCore() = MatchEntryCore(candidateId, score)
}


