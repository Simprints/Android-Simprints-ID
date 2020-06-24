package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep

@Keep
class MatchEntry(val candidateId: String, val score: Float)
