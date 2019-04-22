package com.simprints.id.secure

import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.remote.toDomainToken
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class AuthManager(val client: SecureApiInterface) {

    fun requestAuthToken(authRequest: AuthRequest): Single<AttestToken> {
        return client.requestCustomTokens(
            authRequest.projectId,
            authRequest.userId,
            authRequest.authRequestBody)
            .handleResponse(::handleResponseError)
            .map { it.toDomainToken() }
            .subscribeOn(Schedulers.io())
    }

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            401, 404 -> throw AuthRequestInvalidCredentialsException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
