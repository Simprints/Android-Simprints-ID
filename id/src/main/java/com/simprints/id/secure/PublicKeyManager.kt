package com.simprints.id.secure

import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
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
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
