package com.simprints.feature.clientapi.mappers.request.extractors

internal open class EnrolRequestExtractor(
    extras: Map<String, Any>,
) : ActionRequestExtractor(extras) {
    override val expectedKeys = super.keys
}
