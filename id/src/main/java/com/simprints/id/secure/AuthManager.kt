package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token

interface AuthManager {

    suspend fun requestAuthToken(authRequest: AuthRequest): Token
}
