package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.MatchEntry

class ApiMatchEntry(val candidateId: String, val score: Float) {
    constructor(matchEntry: MatchEntry):
        this(matchEntry.candidateId, matchEntry.score)
}
