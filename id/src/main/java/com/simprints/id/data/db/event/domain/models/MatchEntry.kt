package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

@Keep
data class MatchEntry(val candidateId: String, val score: Float)
