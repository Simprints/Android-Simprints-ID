package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class PublicKeyManager(val client: SecureApiInterface) {

    fun requestPublicKey(projectId: String, userId: String): Single<PublicKeyString> =
        client.requestPublicKey(projectId, userId)
            .handleResponse(::handleResponseError)
            .subscribeOn(Schedulers.io())

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            404 -> throw AuthRequestInvalidCredentialsException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
