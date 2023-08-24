package com.simprints.feature.clientapi.mappers.request.extractors.odk

import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor

class OdkVerifyRequestExtractor(
    extras: Map<String, Any>,
    acceptableExtras: List<String>,
) : VerifyRequestExtractor(extras) {

    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras

}
