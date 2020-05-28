package com.simprints.id.secure

import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent

interface AuthenticationHelper {

    suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String
    ): AuthenticationEvent.Result

}
