package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.MatchEntry

@Keep
class ApiMatchEntry(val candidateId: String, val score: Float) {
    constructor(matchEntry: MatchEntry):
        this(matchEntry.candidateId, matchEntry.score)
}
