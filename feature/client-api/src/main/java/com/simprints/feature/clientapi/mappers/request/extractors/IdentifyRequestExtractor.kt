package com.simprints.feature.clientapi.mappers.request.extractors

internal open class IdentifyRequestExtractor(
    extras: Map<String, Any>,
) : ActionRequestExtractor(extras) {
    override val expectedKeys = super.keys
}
