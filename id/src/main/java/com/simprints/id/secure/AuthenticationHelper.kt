package com.simprints.id.secure

import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result

interface AuthenticationHelper {

    suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String
    ): Result
}
