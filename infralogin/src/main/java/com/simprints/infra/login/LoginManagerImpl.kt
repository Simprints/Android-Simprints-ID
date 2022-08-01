package com.simprints.infra.login

import com.simprints.infra.login.domain.AttestationManager
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource


internal class LoginManagerImpl(
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource,
    private val attestationManager: AttestationManager
) : LoginManager {

    override fun requestAttestation(nonce: String): String =
        attestationManager.requestAttestation(nonce)

    override suspend fun requestAuthenticationData(
        projectId: String,
        userId: String,
        deviceId: String
    ): AuthenticationData =
        authenticationRemoteDataSource.requestAuthenticationData(projectId, userId, deviceId)

    override suspend fun requestAuthToken(
        projectId: String,
        userId: String,
        credentials: AuthRequest
    ): Token = authenticationRemoteDataSource.requestAuthToken(projectId, userId, credentials)

}
