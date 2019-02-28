package com.simprints.clientapi.extensions

import com.simprints.moduleinterfaces.clientapi.responses.ClientIdentifyResponse.Identification


fun List<Identification>.getIdsString(): String {
    return this.joinToString(separator = "", transform = { "${it.guid} " }).trimEnd()
}

fun List<Identification>.getConfidencesString(): String {
    return this.joinToString(separator = "", transform = { "${it.confidence} " }).trimEnd()
}

fun List<Identification>.getTiersString(): String {
    return this.joinToString(separator = "", transform = { "${it.tier} " }).trimEnd()
}
