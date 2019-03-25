package com.simprints.clientapi.extensions

import com.simprints.clientapi.domain.responses.entities.MatchResult

fun List<MatchResult>.getIdsString(): String {
    return this.joinToString(separator = "", transform = { "${it.guidFound} " }).trimEnd()
}

fun List<MatchResult>.getConfidencesString(): String {
    return this.joinToString(separator = "", transform = { "${it.confidence} " }).trimEnd()
}

fun List<MatchResult>.getTiersString(): String {
    return this.joinToString(separator = "", transform = { "${it.tier} " }).trimEnd()
}
