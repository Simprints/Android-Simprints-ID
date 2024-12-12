package com.simprints.feature.clientapi.mappers.request.extractors.odk

import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor

internal class OdkIdentifyRequestExtractor(
    extras: Map<String, Any>,
    acceptableExtras: List<String>,
) : IdentifyRequestExtractor(extras) {
    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras
}
