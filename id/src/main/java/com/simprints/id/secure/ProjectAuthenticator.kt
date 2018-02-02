package com.simprints.id.secure

import com.simprints.id.secure.domain.AuthRequest
import com.simprints.id.secure.domain.NonceScope
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

    fun authenticate(projectId: String, nonceScope: NonceScope, projectSecret: String? = null): Single<String> =
        Single.zip(
            getEncryptedProjectSecret(projectSecret),
            getGoogleAttestation(nonceScope),
            combineAuthParameters(projectId)
        ).makeAuthRequest()

    private fun getEncryptedProjectSecret(projectSecret: String? = null): Single<String> =
        if (projectSecret == null)
            ProjectSecretManager.getEncryptedProjectSecret()
        else PublicKeyManager().requestPublicKey()
            .flatMap { publicKey -> ProjectSecretManager.encryptAndStoreProjectSecret(projectSecret, publicKey) }

    private fun getGoogleAttestation(noneScope: NonceScope): Single<JSONObject>? =
        NonceManager.requestNonce(noneScope).flatMap { nonce ->
            GoogleManager.requestAttestation(nonce)
        }

    private fun combineAuthParameters(projectId: String): BiFunction<String, JSONObject, AuthRequest> =
        BiFunction { encryptedProjectSecret: String, attestation: JSONObject ->
            AuthRequest(encryptedProjectSecret, projectId, attestation)
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<String> =
        flatMap { authRequest -> AuthManager.requestAuthToken(authRequest) }
}
