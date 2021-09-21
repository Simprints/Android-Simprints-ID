package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.MatchEntry

@Keep
data class ApiMatchEntry(val candidateId: String, val score: Float) {
    constructor(matchEntry: MatchEntry) :
        this(matchEntry.candidateId, matchEntry.score)
}
