package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.remote.toDomainAuthData
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class AuthenticationDataManager(val client: SecureApiInterface) {
    fun requestAuthenticationData(projectId: String, userId: String): Single<AuthenticationData> {
        return client.requestAuthenticationData(
            projectId,
            userId)
            .handleResponse(::handleResponseError)
            .map { it.toDomainAuthData() }
            .subscribeOn(Schedulers.io())
    }

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            401, 404 -> throw AuthRequestInvalidCredentialsException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
