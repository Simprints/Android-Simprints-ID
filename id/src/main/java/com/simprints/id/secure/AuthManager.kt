package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token
import io.reactivex.Single

class AuthManager(val client: ApiServiceInterface) {

    fun requestAuthToken(authRequest: AuthRequest): Single<Token> {
        val headers = convertAuthRequestIntoMap(authRequest)
        return client.auth(headers)
    }

    private fun convertAuthRequestIntoMap(authRequest: AuthRequest): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["X-ProjectId"] = authRequest.projectId
        headers["X-UserId"] = authRequest.userId
        headers["X-EncryptedProjectSecret"] = authRequest.encryptedProjectSecret
        headers["X-AttestationResult"] = authRequest.attestation.value
        return headers
    }
}
