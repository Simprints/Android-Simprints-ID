package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.domain.responses.entities.MatchResult

internal fun List<MatchResult>.getIdsString(): String =
    this.map { it.guidFound }.joinToString(separator = " ").trimEnd()

internal fun List<MatchResult>.getConfidencesScoresString(): String =
    this.map { it.confidenceScore }.joinToString(separator = " ").trimEnd()

internal fun List<MatchResult>.getTiersString(): String =
    this.map { it.tier }.joinToString(separator = " ").trimEnd()

internal fun List<MatchResult>.getConfidencesFlagsString(): String =
    this.map { it.matchConfidence }.joinToString(separator = " ").trimEnd()
