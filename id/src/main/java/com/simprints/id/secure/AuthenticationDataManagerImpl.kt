package com.simprints.id.secure

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.remote.toDomainAuthData
import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import retrofit2.HttpException

class AuthenticationDataManagerImpl(
    private val simApiClientFactory: SimApiClientFactory,
    private val deviceId: String
) : AuthenticationDataManager {

    override suspend fun requestAuthenticationData(
        projectId: String,
        userId: String
    ): AuthenticationData =
        try {
            getSecureApiClient().executeCall { remoteInterface ->
                remoteInterface.requestAuthenticationData(projectId, userId, deviceId)
            }.toDomainAuthData()
        } catch (e: Exception) {
            if (e is SyncCloudIntegrationException && e.httpStatusCode() == 404)
                throw AuthRequestInvalidCredentialsException()
            else
                throw e
        }

    private fun getSecureApiClient(): SimApiClient<SecureApiInterface> =
        simApiClientFactory.buildUnauthenticatedClient(SecureApiInterface::class)

}
