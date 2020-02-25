package com.simprints.id.data.db.session.domain.events

import androidx.annotation.Keep

@Keep
class MatchEntry(val candidateId: String, val score: Float)
