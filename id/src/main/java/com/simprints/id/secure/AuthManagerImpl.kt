package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token
import com.simprints.id.secure.models.remote.ApiToken
import com.simprints.id.secure.models.remote.toDomainToken
import retrofit2.HttpException
import retrofit2.Response

class AuthManagerImpl(private val simApiClientFactory: SimApiClientFactory) : AuthManager {

    override suspend fun requestAuthToken(authRequest: AuthRequest): Token {
        val response = executeCall("requestAuthToken") {
            it.requestCustomTokens(authRequest.projectId,
                authRequest.userId,
                authRequest.authRequestBody)
        }

        response.body()?.let {
            return it.toDomainToken()
        } ?: handleResponseError(response)
    }

    private fun handleResponseError(response: Response<ApiToken>): Nothing =
        when (response.code()) {
            401, 404 -> throw AuthRequestInvalidCredentialsException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw HttpException(response)
        }

    private suspend fun <T> executeCall(nameCall: String, block: suspend (SecureApiInterface) -> T): T =
        with(getSecureApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    private fun getSecureApiClient(): SimApiClient<SecureApiInterface> =
        simApiClientFactory.buildUnauthenticatedClient(SecureApiInterface::class)
}
