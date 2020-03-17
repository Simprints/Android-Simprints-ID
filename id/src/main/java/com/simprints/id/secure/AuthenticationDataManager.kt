package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.remote.ApiAuthenticationData
import com.simprints.id.secure.models.remote.toDomainAuthData
import com.simprints.id.tools.utils.retrySimNetworkCalls
import retrofit2.HttpException
import retrofit2.Response

class AuthenticationDataManager(val client: SecureApiInterface) {
    suspend fun requestAuthenticationData(projectId: String, userId: String): AuthenticationData {
        val response = makeNetworkRequest({
            it.requestAuthenticationData(projectId, userId)
        }, "requestAuthData")

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

    private suspend fun <T> makeNetworkRequest(block: suspend (client: SecureApiInterface) -> T, traceName: String): T =
        retrySimNetworkCalls(client, block, traceName)
}
