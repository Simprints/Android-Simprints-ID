package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.exceptions.InvalidVerifyIdException
import com.simprints.core.tools.utils.isValidGuid

class VerifyValidator(val extractor: VerifyExtractor) : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        super.validateClientRequest()
        validateVerifyGuid(extractor.getVerifyGuid())
    }

    private fun validateVerifyGuid(verifyGuid: String?) {
        if (verifyGuid.isNullOrBlank() || !verifyGuid.isValidGuid())
            throw InvalidVerifyIdException("Invalid verify ID")
    }

}
