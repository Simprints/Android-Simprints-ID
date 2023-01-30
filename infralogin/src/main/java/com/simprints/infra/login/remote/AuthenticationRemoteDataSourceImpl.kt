package com.simprints.infra.login.remote

import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.login.remote.models.ApiAuthRequestBody
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import javax.inject.Inject

internal class AuthenticationRemoteDataSourceImpl @Inject constructor(private val simApiClientFactory: SimApiClientFactory) :
    AuthenticationRemoteDataSource {

    companion object {
        private const val UNAUTHORIZED_STATUS_CODE = 401
        private const val NOT_FOUND_STATUS_CODE = 404
    }

    override suspend fun requestAuthenticationData(
        projectId: String,
        userId: String,
        deviceId: String
    ): AuthenticationData =
        try {
            getApiClient().executeCall {
                it.requestAuthenticationData(projectId, userId, deviceId)
            }.toDomain()
        } catch (e: Exception) {
            if (e is SyncCloudIntegrationException && e.httpStatusCode() == NOT_FOUND_STATUS_CODE)
                throw AuthRequestInvalidCredentialsException()
            else
                throw e
        }

    override suspend fun requestAuthToken(
        projectId: String,
        userId: String,
        credentials: AuthRequest
    ): Token =
        try {
            getApiClient().executeCall {
                it.requestCustomTokens(
                    projectId,
                    userId,
                    ApiAuthRequestBody.fromDomain(credentials)
                )
            }.toDomain()
        } catch (e: Exception) {
            if (e is SyncCloudIntegrationException && e.httpStatusCode() == UNAUTHORIZED_STATUS_CODE)
                throw AuthRequestInvalidCredentialsException()
            else
                throw e
        }

    private fun getApiClient(): SimNetwork.SimApiClient<AuthenticationRemoteInterface> =
        simApiClientFactory.buildUnauthenticatedClient(AuthenticationRemoteInterface::class)
}
