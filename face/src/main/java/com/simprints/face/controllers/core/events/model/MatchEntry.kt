package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.MatchEntry as MatchEntryCore

@Keep
class MatchEntry(val candidateId: String, val score: Float) {
    fun fromDomainToCore() = MatchEntryCore(candidateId, score)
}


