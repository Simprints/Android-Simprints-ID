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
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.rxkotlin.Singles

class ProjectAuthenticator(secureDataManager: SecureDataManager,
                           private val dataManager: DbManager,
                           private val safetyNetClient: SafetyNetClient,
                           apiClient: ApiServiceInterface = ApiService().api,
                           private val attestationManager: AttestationManager = AttestationManager()) {

    private val projectSecretManager = ProjectSecretManager(secureDataManager)
    private val publicKeyManager = PublicKeyManager(apiClient)
    private val nonceManager = NonceManager(apiClient)
    private val authManager = AuthManager(apiClient)

    fun authenticate(nonceScope: NonceScope, projectSecret: String): Single<Tokens> =
        prepareAuthRequestParameters(nonceScope, projectSecret)
            .makeAuthRequest()
            .initFirebase(nonceScope.projectId)
            .observeOn(AndroidSchedulers.mainThread())

    private fun prepareAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): Single<AuthRequest> {
        val encryptedProjectSecret = getEncryptedProjectSecret(projectSecret)
        val googleAttestation = getGoogleAttestation(safetyNetClient, nonceScope)
        return zipAuthRequestParameters(encryptedProjectSecret, googleAttestation, nonceScope)
    }

    private fun getEncryptedProjectSecret(projectSecret: String): Single<String> =
        publicKeyManager.requestPublicKey()
            .flatMap { publicKey ->
                SingleJust(projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey)) }

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, noneScope: NonceScope): Single<AttestToken> =
        nonceManager.requestNonce(noneScope)
            .flatMap { nonce -> attestationManager.requestAttestation(safetyNetClient, nonce)
        }

    private fun zipAuthRequestParameters(encryptedProjectSecretSingle: Single<String>,
                                         googleAttestationSingle: Single<AttestToken>,
                                         nonceScope: NonceScope): Single<AuthRequest> =
        Singles.zip(encryptedProjectSecretSingle, googleAttestationSingle) {
            encryptedProjectSecret: String, googleAttestation: AttestToken ->
            AuthRequest(encryptedProjectSecret, nonceScope.projectId, nonceScope.userId, googleAttestation)
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
            authManager.requestAuthToken(authRequest)
        }
}
