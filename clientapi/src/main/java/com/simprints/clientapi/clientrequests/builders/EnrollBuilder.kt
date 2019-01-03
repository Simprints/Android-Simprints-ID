package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.requests.ClientEnrollRequest
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientEnrollRequest
import com.simprints.clientapi.clientrequests.validators.EnrollValidator


class EnrollBuilder(private val extractor: EnrollExtractor, validator: EnrollValidator)
    : ClientRequestBuilder(extractor, validator) {

    override fun buildClientRequest(): ClientRequest = ClientEnrollRequest(
        projectId = extractor.getProjectId()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata()
    )

    override fun buildLegacyClientRequest(): ClientRequest = LegacyClientEnrollRequest(
        legacyApiKey = extractor.getLegacyApiKey()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata()
    )

}
