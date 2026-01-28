package com.simprints.infra.authlogic.authenticator.remote

import com.simprints.infra.authlogic.authenticator.remote.models.ApiAuthRequestBody
import com.simprints.infra.authstore.domain.models.AuthRequest
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import javax.inject.Inject

internal class AuthenticationRemoteDataSource @Inject constructor(
    private val backendApiClient: BackendApiClient,
) {
    suspend fun requestAuthenticationData(
        projectId: String,
        deviceId: String,
    ): AuthenticationData = try {
        backendApiClient
            .executeUnauthenticatedCall(AuthenticationRemoteInterface::class) { api -> api.requestAuthenticationData(projectId, deviceId) }
            .getOrThrow()
            .toDomain()
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
        backendApiClient
            .executeUnauthenticatedCall(AuthenticationRemoteInterface::class) { api ->
                api.requestCustomTokens(
                    projectId,
                    deviceId,
                    ApiAuthRequestBody.fromDomain(credentials),
                )
            }.getOrThrow()
            .toDomain()
    } catch (e: Exception) {
        if (e is SyncCloudIntegrationException && e.httpStatusCode() == UNAUTHORIZED_STATUS_CODE) {
            throw AuthRequestInvalidCredentialsException()
        } else {
            throw e
        }
    }

    companion object {
        private const val UNAUTHORIZED_STATUS_CODE = 401
        private const val NOT_FOUND_STATUS_CODE = 404
    }
}
