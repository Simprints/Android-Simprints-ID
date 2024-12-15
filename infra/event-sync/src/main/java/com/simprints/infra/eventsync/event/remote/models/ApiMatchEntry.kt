package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.MatchEntry

@Keep
internal data class ApiMatchEntry(
    val candidateId: String,
    val score: Float,
) {
    constructor(matchEntry: MatchEntry) :
        this(matchEntry.candidateId, matchEntry.score)
}
