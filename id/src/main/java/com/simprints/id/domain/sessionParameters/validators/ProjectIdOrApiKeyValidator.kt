package com.simprints.id.domain.sessionParameters.validators

import com.simprints.id.domain.sessionParameters.SessionParameters

class ProjectIdOrApiKeyValidator(private val errorWhenInvalid: Error) : Validator<SessionParameters>{

    override fun validate(sessionParameters: SessionParameters) {
        if (sessionParameters.apiKey.isEmpty() && sessionParameters.projectId.isEmpty()) {
            throw errorWhenInvalid
        }
    }
}
