package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.core.tools.utils.isValidGuid
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError

internal class VerifyValidator(
    private val extractor: VerifyRequestExtractor,
) : RequestActionValidator(extractor) {
    override fun validate() {
        super.validate()
        validateVerifyGuid(extractor.getVerifyGuid())
    }

    private fun validateVerifyGuid(verifyGuid: String?) {
        if (verifyGuid.isNullOrBlank() || !verifyGuid.isValidGuid()) {
            throw InvalidRequestException("Invalid verify ID", ClientApiError.INVALID_VERIFY_ID)
        }
    }
}
