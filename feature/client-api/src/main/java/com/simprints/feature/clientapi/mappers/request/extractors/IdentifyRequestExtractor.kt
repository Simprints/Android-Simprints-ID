package com.simprints.feature.clientapi.mappers.request.extractors

import com.simprints.libsimprints.Constants

internal open class IdentifyRequestExtractor(
    extras: Map<String, Any>,
) : ActionRequestExtractor(extras) {
    override val expectedKeys = super.keys + listOf(
        Constants.SIMPRINTS_BIOMETRIC_DATA_SOURCE,
    )
}
