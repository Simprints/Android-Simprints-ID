package com.simprints.infra.login.remote

import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token

internal interface AuthenticationRemoteDataSource {

    suspend fun requestAuthenticationData(
        projectId: String,
        userId: String,
        deviceId: String
    ): AuthenticationData

    suspend fun requestAuthToken(
        projectId: String,
        userId: String,
        credentials: AuthRequest
    ): Token
}
