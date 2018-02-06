package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.Token
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

//Temporary to "simulate" an async network for auth - it will be properly implemented soon
class AuthManager(val client: ApiServiceInterface) {

    fun requestAuthToken(authRequest: AuthRequest): Single<Token> {
        val headers = HashMap<String, String>()
        headers["X-ProjectId"] = authRequest.projectId
        headers["X-UserId"] = authRequest.userId
        headers["X-EncryptedProjectSecret"] = authRequest.encryptedProjectSecret
        headers["X-AttestationResult"] = authRequest.attestation.value
        return client.auth(headers)
    }
}
