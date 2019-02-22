package com.simprints.clientapi.extensions

import com.simprints.libsimprints.Identification


fun ArrayList<Identification>.getIdsString(): String {
    return this.joinToString(separator = "", transform = { "${it.guid} " }).trimEnd()
}

fun ArrayList<Identification>.getConfidencesString(): String {
    return this.joinToString(separator = "", transform = { "${it.confidence} " }).trimEnd()
}

fun ArrayList<Identification>.getTiersString(): String {
    return this.joinToString(separator = "", transform = { "${it.tier} " }).trimEnd()
}
