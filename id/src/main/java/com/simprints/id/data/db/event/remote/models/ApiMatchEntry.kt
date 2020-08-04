package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.MatchEntry

@Keep
data class ApiMatchEntry(val candidateId: String, val score: Float) {
    constructor(matchEntry: MatchEntry) :
        this(matchEntry.candidateId, matchEntry.score)
}
