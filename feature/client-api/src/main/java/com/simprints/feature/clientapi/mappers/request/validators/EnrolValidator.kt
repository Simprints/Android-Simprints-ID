package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor

internal class EnrolValidator(
    extractor: EnrolRequestExtractor,
) : RequestActionValidator(extractor)
