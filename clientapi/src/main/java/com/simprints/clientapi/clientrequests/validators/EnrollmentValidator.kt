package com.simprints.clientapi.clientrequests.validators

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.requests.ClientEnrollmentRequest
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException

class EnrollmentValidator(extractor: EnrollmentExtractor) : ClientRequestValidator(extractor) {

    override fun validateClientRequest() {
        if (!hasValidProjectId() && !hasValidApiKey())
            throw InvalidProjectIdException()
        else if (!hasValidUserId())
            throw InvalidUserIdException()
        else if (!hasValidModuleId())
            throw InvalidModuleIdException()
        else if (hasMetadata())
            if (!hasValidMetadata())
                throw InvalidMetadataException()
    }

}
