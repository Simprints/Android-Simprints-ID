package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.domain.Project
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.SimprintsInternalServerException
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.Tokens
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import java.io.IOException

open class ProjectAuthenticator(private val loginInfoManager: LoginInfoManager,
                                private val dbManager: DbManager,
                                private val safetyNetClient: SafetyNetClient,
                                secureApiClient: SecureApiInterface = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api,
                                private val attestationManager: AttestationManager = AttestationManager()) {

    private val projectSecretManager = ProjectSecretManager(loginInfoManager)
    private val publicKeyManager = PublicKeyManager(secureApiClient)
    private val nonceManager = NonceManager(secureApiClient)
    private val authManager = AuthManager(secureApiClient)

    @Throws(
        IOException::class,
        DifferentProjectIdReceivedFromIntentException::class,
        AuthRequestInvalidCredentialsException::class,
        SimprintsInternalServerException::class)
    protected fun authenticate(nonceScope: NonceScope, projectSecret: String): Completable =
        prepareAuthRequestParameters(nonceScope, projectSecret)
            .makeAuthRequest()
            .signIn(nonceScope.projectId)
            .fetchProjectInfo(nonceScope.projectId)
            .storeCredentials(nonceScope.userId)
            .observeOn(AndroidSchedulers.mainThread())

    private fun prepareAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): Single<AuthRequest> {
        val encryptedProjectSecret = getEncryptedProjectSecret(projectSecret)
        val googleAttestation = getGoogleAttestation(safetyNetClient, nonceScope)
        return zipAuthRequestParameters(encryptedProjectSecret, googleAttestation, nonceScope)
    }

    private fun getEncryptedProjectSecret(projectSecret: String): Single<String> =
        publicKeyManager.requestPublicKey()
            .flatMap { publicKey ->
                Single.just(projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey)) }

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

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Tokens> =
        flatMap { authRequest ->
            authManager.requestAuthToken(authRequest)
        }

    private fun Single<out Tokens>.signIn(projectId: String): Completable =
        flatMapCompletable { tokens ->
            dbManager.signIn(projectId, tokens)
        }

    private fun Completable.fetchProjectInfo(projectId: String): Single<Project> =
        andThen(
            dbManager.refreshProjectInfoWithServer(projectId)
        )

    private fun Single<out Project>.storeCredentials(userId: String): Completable =
        flatMapCompletable {
            loginInfoManager.storeCredentials(it.id, it.legacyId, userId)
            Completable.complete()
        }
}
