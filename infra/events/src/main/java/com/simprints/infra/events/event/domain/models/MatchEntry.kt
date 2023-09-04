package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep

// TODO [CORE-2502] Check if candidateId is the same as attendant id
@Keep
data class MatchEntry(val candidateId: String, val score: Float)
