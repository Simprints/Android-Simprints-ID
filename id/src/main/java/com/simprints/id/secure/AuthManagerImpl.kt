package com.simprints.id.secure

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token
import com.simprints.infra.network.SimApiClient
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import retrofit2.HttpException

class AuthManagerImpl(private val simApiClientFactory: SimApiClientFactory) : AuthManager {

    override suspend fun requestAuthToken(authRequest: AuthRequest): Token =
        try {
            getSecureApiClient().executeCall {
                it.requestCustomTokens(
                    authRequest.projectId,
                    authRequest.userId,
                    authRequest.authRequestBody
                )
            }.toDomainToken()
        } catch (e: Exception) {
            if (e is SyncCloudIntegrationException && (e.cause is HttpException) && (e.cause as HttpException).code() == 401)
                throw AuthRequestInvalidCredentialsException()
            else
                throw e
        }

    private fun getSecureApiClient(): SimApiClient<SecureApiInterface> =
        simApiClientFactory.buildUnauthenticatedClient(SecureApiInterface::class)

}
