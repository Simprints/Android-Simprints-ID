package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Tokens
import io.reactivex.Single

class AuthManager(val client: ApiServiceInterface) {

    fun requestAuthToken(authRequest: AuthRequest): Single<Tokens> {
        val headers = mapOf(
            "X-ProjectId" to authRequest.projectId,
            "X-UserId" to authRequest.userId,
            "X-EncryptedProjectSecret" to authRequest.encryptedProjectSecret,
            "X-AttestationResult" to authRequest.attestation.value)

        return client.auth(headers)
    }
}
