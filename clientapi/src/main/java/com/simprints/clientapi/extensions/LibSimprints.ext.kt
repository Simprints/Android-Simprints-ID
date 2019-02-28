package com.simprints.clientapi.extensions

import com.simprints.moduleinterfaces.clientapi.responses.IClientApiIdentifyResponse.IIdentificationResult


fun List<IIdentificationResult>.getIdsString(): String {
    return this.joinToString(separator = "", transform = { "${it.guid} " }).trimEnd()
}

fun List<IIdentificationResult>.getConfidencesString(): String {
    return this.joinToString(separator = "", transform = { "${it.confidence} " }).trimEnd()
}

fun List<IIdentificationResult>.getTiersString(): String {
    return this.joinToString(separator = "", transform = { "${it.tier} " }).trimEnd()
}
