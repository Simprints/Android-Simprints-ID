package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.simprintsrequests.requests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.requests.VerifyRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacySimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacyVerifyRequest


class VerifyBuilder(private val extractor: VerifyExtractor, validator: VerifyValidator)
    : ClientRequestBuilder(extractor, validator) {

    override fun buildSimprintsRequest(): SimprintsIdRequest = VerifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata(),
        verifyGuid = extractor.getVerifyGuid()
    )

    override fun buildLegacySimprintsRequest(): LegacySimprintsIdRequest = LegacyVerifyRequest(
        legacyApiKey = extractor.getLegacyApiKey(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetatdata(),
        verifyGuid = extractor.getVerifyGuid()
    )

}
