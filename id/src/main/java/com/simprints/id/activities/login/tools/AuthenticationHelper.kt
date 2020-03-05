package com.simprints.id.activities.login.tools

import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent

interface AuthenticationHelper {

    suspend fun authenticateSafely(
        authBlock: suspend () -> Unit,
        after: (result: AuthenticationEvent.Result) -> Unit
    ): AuthenticationEvent.Result

}
