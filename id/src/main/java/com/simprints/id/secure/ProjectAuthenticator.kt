package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.Tokens
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.schedulers.Schedulers

class ProjectAuthenticator(secureDataManager: SecureDataManager,
                           private val dataManager: DbManager,
                           private val safetyNetClient: SafetyNetClient,
                           apiClient: ApiServiceInterface = ApiService().api,
                           private val attestationManager: AttestationManager = AttestationManager()) {

    private val projectSecretManager = ProjectSecretManager(secureDataManager)
    private val publicKeyManager = PublicKeyManager(apiClient)
    private val nonceManager = NonceManager(apiClient)
    private val authManager = AuthManager(apiClient)

    fun authenticateWithNewCredentials(nonceScope: NonceScope, projectSecret: String): Single<Tokens> =
        authenticate(nonceScope, getEncryptedProjectSecret(projectSecret))

    private fun authenticate(nonceScope: NonceScope, encryptedProjectSecret: Single<String>): Single<Tokens> =
        combineProjectSecretAndGoogleAttestationObservables(nonceScope, encryptedProjectSecret)
            .makeAuthRequest()
            .initFirebase(nonceScope.projectId)
            .observeOn(AndroidSchedulers.mainThread())

    private fun combineProjectSecretAndGoogleAttestationObservables(nonceScope: NonceScope, encryptedProjectSecret: Single<String>): Single<AuthRequest> =
        Single.zip(
            encryptedProjectSecret.subscribeOn(Schedulers.io()),
            getGoogleAttestation(safetyNetClient, nonceScope).subscribeOn(Schedulers.io()),
            combineAuthRequestParameters(nonceScope.projectId, nonceScope.userId)
        )

    private fun getEncryptedProjectSecret(projectSecret: String): Single<String> =
        publicKeyManager.requestPublicKey()
            .flatMap { publicKey -> projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey) }

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, noneScope: NonceScope): Single<AttestToken> =
        nonceManager.requestNonce(noneScope).flatMap { nonce ->
            attestationManager
                .requestAttestation(safetyNetClient, nonce)
                .subscribeOn(Schedulers.io())
        }

    private fun combineAuthRequestParameters(projectId: String, userId: String): BiFunction<String, AttestToken, AuthRequest> =
        BiFunction { encryptedProjectSecret: String, attestation: AttestToken ->
            AuthRequest(encryptedProjectSecret, projectId, userId, attestation)
        }

    private fun Single<out Tokens>.initFirebase(projectId: String): Single<Tokens> =
        flatMap { tokens ->
            if (!dataManager.isDbInitialised(projectId)) {
                dataManager.initialiseDb(projectId)
            }

            //TODO: Fix it when we implement the 2 firebase apps in FirebaseManager
            dataManager.signIn(projectId, tokens)
            SingleJust(tokens)
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Tokens> =
        flatMap { authRequest ->
            authManager
            .requestAuthToken(authRequest)
            .subscribeOn(Schedulers.io())
        }
}
