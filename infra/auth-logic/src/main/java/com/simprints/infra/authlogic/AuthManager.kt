package com.simprints.infra.authlogic

import com.simprints.infra.authlogic.model.AuthenticateDataResult

// TODO This module is just a collection of all the public auth/login related
//      business logic pulled together as-is without proper review and refactoring.
//      It would make sense to eventually review and potentially simplify internal
//      auth logic and public API of this module - CORE-2589.
interface AuthManager {
    suspend fun authenticateSafely(
        userId: String,
        projectId: String,
        projectSecret: String,
        deviceId: String,
    ): AuthenticateDataResult

    suspend fun signOut()
}
