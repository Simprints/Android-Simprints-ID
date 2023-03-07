package com.simprints.infra.events.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.MatchEntry

@Keep
data class ApiMatchEntry(val candidateId: String, val score: Float) {
    constructor(matchEntry: MatchEntry) :
        this(matchEntry.candidateId, matchEntry.score)
}
