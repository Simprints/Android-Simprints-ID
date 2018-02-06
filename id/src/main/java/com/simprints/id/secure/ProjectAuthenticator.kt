package com.simprints.id.secure

import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.NonceScope
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

//      CALLER
// ProjectAuthenticator().authenticate(projectId, nonceScope, projectSecret?).subscribe(
//            { token -> print("we got it!!! $token") },
//            { e -> throw e }
//        )

// Working in progress
class ProjectAuthenticator() {

    val apiClient = ApiService().api

    fun authenticate(projectId: String, nonceScope: NonceScope, projectSecret: String? = null): Single<String> =
        Single.zip(
            getEncryptedProjectSecret(projectSecret),
            getGoogleAttestation(nonceScope),
            combineAuthParameters(projectId)
        ).makeAuthRequest()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun getEncryptedProjectSecret(projectSecret: String? = null): Single<String> =
        if (projectSecret == null)
            ProjectSecretManager.getEncryptedProjectSecret()
        else PublicKeyManager(apiClient).requestPublicKey()
            .flatMap { publicKey -> ProjectSecretManager.encryptAndStoreProjectSecret(projectSecret, publicKey) }

    private fun getGoogleAttestation(noneScope: NonceScope): Single<JSONObject>? {
        return NonceManager(apiClient).requestNonce(noneScope).flatMap { nonce ->
            GoogleManager.requestAttestation(nonce)
        }
    }

    private fun combineAuthParameters(projectId: String): BiFunction<String, JSONObject, AuthRequest> =
        BiFunction { encryptedProjectSecret: String, attestation: JSONObject ->
            AuthRequest(encryptedProjectSecret, projectId, attestation)
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<String> =
        flatMap { authRequest -> AuthManager.requestAuthToken(authRequest) }
}
