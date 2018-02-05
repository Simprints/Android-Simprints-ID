package com.simprints.id.secure

import android.app.Activity
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.Token
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

//      CALLER
// ProjectAuthenticator().authenticate(projectId, nonceScope, projectSecret?).subscribe(
//            { token -> print("we got it!!! $token") },
//            { e -> throw e }
//        )

// Working in progress
class ProjectAuthenticator() {

    val apiClient = ApiService().api

    fun authenticate(act: Activity,
                     nonceScope: NonceScope,
                     projectSecret: String? = null): Single<Token> =
        Single.zip(
            getEncryptedProjectSecret(projectSecret),
            getGoogleAttestation(act, nonceScope),
            combineAuthParameters(nonceScope.projectId, nonceScope.userId)
        ).makeAuthRequest()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun getEncryptedProjectSecret(projectSecret: String? = null): Single<String> =
        if (projectSecret == null)
            ProjectSecretManager.getEncryptedProjectSecret()
        else PublicKeyManager(apiClient).requestPublicKey()
            .flatMap { publicKey -> ProjectSecretManager.encryptAndStoreProjectSecret(projectSecret, publicKey) }

    private fun getGoogleAttestation(act: Activity, noneScope: NonceScope): Single<AttestToken>? {
        return NonceManager(apiClient).requestNonce(noneScope).flatMap { nonce ->
            GoogleManager().requestAttestation(act, nonce)
        }
    }

    private fun combineAuthParameters(projectId: String, userId: String): BiFunction<String, AttestToken, AuthRequest> =
        BiFunction { encryptedProjectSecret: String, attestation: AttestToken ->
            AuthRequest(encryptedProjectSecret, projectId, userId, attestation)
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Token> =
        flatMap { authRequest -> AuthManager(apiClient).requestAuthToken(authRequest) }
}
