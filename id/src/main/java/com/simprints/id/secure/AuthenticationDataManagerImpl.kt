package com.simprints.id.secure

import com.simprints.id.exceptions.safe.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.toDomainAuthData
import retrofit2.HttpException
import retrofit2.Response

class AuthenticationDataManagerImpl(
    private val simApiClientFactory: SimApiClientFactory,
    private val deviceId: String
): AuthenticationDataManager {

    override suspend fun requestAuthenticationData(projectId: String, userId: String): AuthenticationData {
        val response = executeCall("requestAuthData") {
            it.requestAuthenticationData(projectId, userId, deviceId)
        }

        response.body()?.let {
            return it.toDomainAuthData()
        } ?: handleResponseError(response)
    }

    private fun handleResponseError(response: Response<ApiAuthenticationData>): Nothing =
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
