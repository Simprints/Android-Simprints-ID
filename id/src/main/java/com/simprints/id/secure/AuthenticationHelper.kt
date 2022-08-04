package com.simprints.id.secure

import com.simprints.id.secure.models.AuthenticateDataResult

interface AuthenticationHelper {

    suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String
    ): AuthenticateDataResult
}
