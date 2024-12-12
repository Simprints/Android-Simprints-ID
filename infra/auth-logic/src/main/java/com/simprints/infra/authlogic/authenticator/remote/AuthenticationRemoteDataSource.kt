package com.simprints.infra.authlogic.authenticator.remote

import com.simprints.infra.authlogic.authenticator.remote.models.ApiAuthRequestBody
import com.simprints.infra.authstore.domain.models.AuthRequest
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import javax.inject.Inject

internal class AuthenticationRemoteDataSource @Inject constructor(
    private val apiClientFactory: UnauthenticatedClientFactory,
) {
    suspend fun requestAuthenticationData(
        projectId: String,
        deviceId: String,
    ): AuthenticationData = try {
        getApiClient()
            .executeCall {
                it.requestAuthenticationData(projectId, deviceId)
            }.toDomain()
    } catch (e: Exception) {
        if (e is SyncCloudIntegrationException && e.httpStatusCode() == NOT_FOUND_STATUS_CODE) {
            throw AuthRequestInvalidCredentialsException()
        } else {
            throw e
        }
    }

    suspend fun requestAuthToken(
        projectId: String,
        deviceId: String,
        credentials: AuthRequest,
    ): Token = try {
        getApiClient()
            .executeCall {
                it.requestCustomTokens(
                    projectId,
                    deviceId,
                    ApiAuthRequestBody.fromDomain(credentials),
                )
            }.toDomain()
    } catch (e: Exception) {
        if (e is SyncCloudIntegrationException && e.httpStatusCode() == UNAUTHORIZED_STATUS_CODE) {
            throw AuthRequestInvalidCredentialsException()
        } else {
            throw e
        }
    }

    private fun getApiClient(): SimNetwork.SimApiClient<AuthenticationRemoteInterface> =
        apiClientFactory.build(AuthenticationRemoteInterface::class)

    companion object {
        private const val UNAUTHORIZED_STATUS_CODE = 401
        private const val NOT_FOUND_STATUS_CODE = 404
    }
}
