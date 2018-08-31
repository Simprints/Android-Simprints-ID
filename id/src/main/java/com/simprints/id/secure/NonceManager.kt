package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.tools.extensions.handleResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class NonceManager(val client: SecureApiInterface) {

    fun requestNonce(nonceScope: NonceScope): Single<Nonce> {
        return client.requestNonce(nonceScope.projectId, nonceScope.userId)
            .handleResponse(::handleResponseError)
            .subscribeOn(Schedulers.io())
    }

    private fun handleResponseError(e: HttpException): Nothing =
        when (e.code()) {
            404 -> throw AuthRequestInvalidCredentialsException()
            in 500..599 -> throw SimprintsInternalServerException()
            else -> throw e
        }
}
