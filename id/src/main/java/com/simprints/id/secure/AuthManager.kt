package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Tokens
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AuthManager(val client: ApiServiceInterface) {

    companion object {
        const val projectIdHeaderKey = "X-ProjectId"
        const val userIdHeaderKey = "X-UserId"
        private const val encryptedProjectSecretHeaderKey = "X-EncryptedProjectSecret"
        private const val attestationResultHeaderKey = "X-AttestationResult"
    }

    fun requestAuthToken(authRequest: AuthRequest): Single<Tokens> {
        val headers = mapOf(
            projectIdHeaderKey to authRequest.projectId,
            userIdHeaderKey to authRequest.userId,
            encryptedProjectSecretHeaderKey to authRequest.encryptedProjectSecret,
            attestationResultHeaderKey to authRequest.attestation.value)

        return client.auth(headers).subscribeOn(Schedulers.io())
    }
}
