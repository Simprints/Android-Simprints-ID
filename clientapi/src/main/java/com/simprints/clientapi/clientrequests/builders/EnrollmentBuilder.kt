package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.requests.ClientEnrollmentRequest
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.clientrequests.requests.legacy.LegacyClientEnrollmentRequest
import com.simprints.clientapi.clientrequests.validators.EnrollmentValidator


class EnrollmentBuilder(private val extractor: EnrollmentExtractor, validator: EnrollmentValidator)
    : ClientRequestBuilder(extractor, validator) {

    override fun buildClientRequest(): ClientRequest = ClientEnrollmentRequest(
        projectId = extractor.getProjectId()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata()
    )

    override fun buildLegacyClientRequest(): ClientRequest = LegacyClientEnrollmentRequest(
        legacyApiKey = extractor.getLegacyApiKey()!!,
        userId = extractor.getUserId()!!,
        moduleId = extractor.getModuleId()!!,
        metadata = extractor.getMetatdata()
    )

}
