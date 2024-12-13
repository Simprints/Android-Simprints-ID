package com.simprints.feature.clientapi.mappers.request.extractors.odk

import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor

internal class OdkEnrolRequestExtractor(
    extras: Map<String, Any>,
    acceptableExtras: List<String>,
) : EnrolRequestExtractor(extras) {
    override val expectedKeys: List<String> = super.expectedKeys + acceptableExtras
}
