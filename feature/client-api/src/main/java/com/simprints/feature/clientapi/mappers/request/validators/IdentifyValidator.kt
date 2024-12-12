package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor

internal class IdentifyValidator(
    extractor: IdentifyRequestExtractor,
) : RequestActionValidator(extractor)
