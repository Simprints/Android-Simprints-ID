package com.simprints.feature.clientapi.mappers.request.validators

import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyIdentityRequestExtractor
import com.simprints.feature.clientapi.models.ClientApiError

// TODO PoC
internal class VerifyIdentityValidator(
    private val extractor: VerifyIdentityRequestExtractor,
) : RequestActionValidator(extractor) {

    override fun validate() {
        super.validate()
        validateVerifyUri(extractor.getImage())
    }

    private fun validateVerifyUri(verifyGuid: String?) {
        if (verifyGuid.isNullOrBlank())
            throw InvalidRequestException("Invalid Uri", ClientApiError.INVALID_METADATA)
    }

}
