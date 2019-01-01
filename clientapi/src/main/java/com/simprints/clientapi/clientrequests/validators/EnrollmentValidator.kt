package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor

class EnrollmentValidator(extractor: EnrollmentExtractor) : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        validateProjectIdOrLegacyApiKey()
        validateUserId()
        validateModuleId()
        validateMetadata()
    }

}
