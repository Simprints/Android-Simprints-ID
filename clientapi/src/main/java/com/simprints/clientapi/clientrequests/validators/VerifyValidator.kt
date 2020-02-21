package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.exceptions.InvalidVerifyIdException

class VerifyValidator(val extractor: VerifyExtractor) : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        super.validateClientRequest()
        validateVerifyGuid(extractor.getVerifyGuid())
    }

    private fun validateVerifyGuid(verifyGuid: String?) {
        if (verifyGuid.isNullOrBlank() || !verifyGuid.isValidGuid())
            throw InvalidVerifyIdException("Invalid verify ID")
    }

    /**
     * "A UUID is made of up of hex digits  (4 chars each) along with 4 “-”
     * symbols which make its length equal to 36 characters"
     *
     * @see [https://www.baeldung.com/java-uuid]
     * @see [java.util.UUID]
     */
    private fun String.isValidGuid(): Boolean = this.matches(REGEX_GUID.toRegex())

    private companion object {
        const val REGEX_GUID =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"
    }

}
