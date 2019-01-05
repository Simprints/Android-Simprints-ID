package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.simprintsrequests.IdentifyRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacySimprintsIdRequest


class IdentifyBuilder(val extractor: IdentifyExtractor, validator: IdentifyValidator) :
    ClientRequestBuilder(extractor, validator) {

    override fun buildSimprintsRequest(): SimprintsIdRequest = IdentifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

    override fun buildLegacySimprintsRequest(): LegacySimprintsIdRequest = LegacyIdentifyRequest(
        legacyApiKey = extractor.getLegacyApiKey(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata()
    )

}
