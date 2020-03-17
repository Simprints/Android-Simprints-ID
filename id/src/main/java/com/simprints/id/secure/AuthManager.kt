package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token
import com.simprints.id.secure.models.remote.toDomainToken
import com.simprints.id.tools.utils.retrySimNetworkCalls
import retrofit2.HttpException

class AuthManager(val client: SecureApiInterface) {

    suspend fun requestAuthToken(authRequest: AuthRequest): Token {
        val response = makeNetworkRequest({
            it.requestCustomTokens(authRequest.projectId,
                authRequest.userId,
                authRequest.authRequestBody)
        }, "requestAuthToken")
        return response.body()!!.toDomainToken()
    }

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            401, 404 -> throw AuthRequestInvalidCredentialsException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }

    private suspend fun <T> makeNetworkRequest(block: suspend (client: SecureApiInterface) -> T, traceName: String): T =
        retrySimNetworkCalls(client, block, traceName)
}
