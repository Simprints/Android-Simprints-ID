package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.MatchEntry as MatchEntryCore

/**
 * This class represents a candidate for a matching fingerprint
 *
 * @property candidateId  the unique id representing the matching subject
 * @property score  the score representing the percentage for the match
 */
@Keep
class MatchEntry(val candidateId: String, val score: Float)

fun MatchEntry.fromDomainToCore() =
    MatchEntryCore(candidateId, score)
