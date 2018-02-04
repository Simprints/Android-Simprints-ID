package com.simprints.id.secure

import com.simprints.id.secure.models.NonceScope
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.single.SingleJust
import org.json.JSONObject

//      CALLER
// this.takeToken(projectId, projectSecret_).subscribe(
//            { token -> print("we got it!!! $token") },
//            { e -> throw e }
//        )

class ProjectAuthenticator {

    val apiClient = ApiService().api

    fun authenticate(projectId: String, nonceScope: NonceScope, projectSecret: String? = null): Single<String> =
        Single.zip(
            getEncryptedProjectSecret(projectSecret),
            getGoogleAttestation(nonceScope),
            combineAuthParameters(projectId)
        ).makeAuthRequest()

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<String> =
        flatMap { authRequest -> AuthManager.requestAuthToken(authRequest) }

    private fun combineAuthParameters(projectId: String): BiFunction<String, JSONObject, AuthRequest> {
        return BiFunction { encryptedProjectSecret: String, attestation: JSONObject ->
            AuthRequest(encryptedProjectSecret, projectId, attestation)
        }
    }

    private fun getGoogleAttestation(noneScope: NonceScope): Single<JSONObject>? {
        return NonceManager(apiClient).requestNonce(noneScope).flatMap { nonce ->
            GoogleManager.requestAttestation(nonce)
        }
    }

    private fun getEncryptedProjectSecret(projectSecret: String? = null): Single<String> =
        if (projectSecret == null)
            readSharedPreferences() //TODO: Exception if project Secret encrypted is not shared
        else PublicKeyManager().requestPublicKey()
            .flatMap { publicKey -> PublicKeyManager().encryptProjectSecret(projectSecret, publicKey) }

    private fun readSharedPreferences(): Single<String> {
        return SingleJust<String>("")
    }
}
