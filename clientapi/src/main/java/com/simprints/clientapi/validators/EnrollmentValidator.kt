package com.simprints.clientapi.validators

import android.content.Intent
import com.simprints.clientapi.exceptions.InvalidMetadataException
import com.simprints.clientapi.exceptions.InvalidModuleIdException
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.exceptions.InvalidUserIdException

class EnrollmentValidator(intent: Intent) : ClientRequestValidator(intent) {

    override fun validateClientRequest() {
        if (!hasValidProjectId() || !hasValidApiKey())
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
