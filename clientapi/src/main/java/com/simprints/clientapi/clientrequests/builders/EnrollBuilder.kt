package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.simprintsrequests.requests.EnrollRequest
import com.simprints.clientapi.simprintsrequests.requests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacyEnrollRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacySimprintsIdRequest


class EnrollBuilder(private val extractor: EnrollExtractor, validator: EnrollValidator)
    : ClientRequestBuilder(extractor, validator) {

    override fun buildSimprintsRequest(): SimprintsIdRequest = EnrollRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

    override fun buildLegacySimprintsRequest(): LegacySimprintsIdRequest = LegacyEnrollRequest(
        legacyApiKey = extractor.getLegacyApiKey(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

}
