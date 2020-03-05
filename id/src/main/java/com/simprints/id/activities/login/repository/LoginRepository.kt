package com.simprints.id.activities.login.repository

import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent

interface LoginRepository {

    suspend fun authenticate(
        projectId: String,
        userId: String,
        projectSecret: String
    ): AuthenticationEvent.Result

}
