package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.clientrequests.requests.ClientVerifyRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientVerifyRequest
import com.simprints.clientapi.clientrequests.validators.VerifyValidator


class VerifyBuilder(private val extractor: VerifyExtractor, validator: VerifyValidator)
    : ClientRequestBuilder(extractor, validator) {

    override fun buildClientRequest(): ClientRequest = ClientVerifyRequest(
        projectId = extractor.getProjectId()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata(),
        verifyGuid = extractor.getVerifyGuid()!!
    )

    override fun buildLegacyClientRequest(): LegacyClientRequest = LegacyClientVerifyRequest(
        legacyApiKey = extractor.getLegacyApiKey()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata(),
        verifyGuid = extractor.getVerifyGuid()!!
    )

}
