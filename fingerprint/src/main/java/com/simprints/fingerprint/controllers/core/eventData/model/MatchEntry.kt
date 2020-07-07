package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.MatchEntry as MatchEntryCore

@Keep
class MatchEntry(val candidateId: String, val score: Float)

fun MatchEntry.fromDomainToCore() =
    MatchEntryCore(candidateId, score)
