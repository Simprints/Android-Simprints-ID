package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.Project
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.models.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

open class ProjectAuthenticator(component: AppComponent,
                                private val safetyNetClient: SafetyNetClient,
                                secureApiClient: SecureApiInterface = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api,
                                private val attestationManager: AttestationManager = AttestationManager()) {

    @Inject lateinit var secureDataManager: SecureDataManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteConfigWrapper: RemoteConfigWrapper

    private val projectSecretManager by lazy { ProjectSecretManager(loginInfoManager) }
    private val publicKeyManager = PublicKeyManager(secureApiClient)
    private val nonceManager = NonceManager(secureApiClient)
    private val authManager = AuthManager(secureApiClient)

 	init {
        component.inject(this)
    }

    /**
     * @throws IOException
     * @throws DifferentProjectIdReceivedFromIntentException
     * @throws AuthRequestInvalidCredentialsException
     * @throws SimprintsInternalServerException
     */
    fun authenticate(nonceScope: NonceScope, projectSecret: String): Completable =
        prepareAuthRequestParameters(nonceScope, projectSecret)
            .makeAuthRequest()
            .signIn(nonceScope.projectId)
            .fetchProjectInfo(nonceScope.projectId)
            .storeCredentials(nonceScope.userId)
            .fetchProjectRemoteConfigSettings(nonceScope.projectId)
            .storeProjectRemoteConfigSettings()
            .observeOn(AndroidSchedulers.mainThread())

    private fun prepareAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): Single<AuthRequest> {
        val encryptedProjectSecret = getEncryptedProjectSecret(projectSecret, nonceScope)
        val googleAttestation = getGoogleAttestation(safetyNetClient, nonceScope)
        return zipAuthRequestParameters(encryptedProjectSecret, googleAttestation, nonceScope)
    }

    private fun getEncryptedProjectSecret(projectSecret: String, noneScope: NonceScope): Single<String> =
        publicKeyManager.requestPublicKey(noneScope.projectId, noneScope.userId)
            .flatMap { publicKey ->
                Single.just(projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey))
            }

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, noneScope: NonceScope): Single<AttestToken> =
        nonceManager.requestNonce(noneScope)
            .flatMap { nonce ->
                attestationManager.requestAttestation(safetyNetClient, nonce)
            }

    private fun zipAuthRequestParameters(encryptedProjectSecretSingle: Single<String>,
                                         googleAttestationSingle: Single<AttestToken>,
                                         nonceScope: NonceScope): Single<AuthRequest> =
        Singles.zip(encryptedProjectSecretSingle, googleAttestationSingle) {
            encryptedProjectSecret: String, googleAttestation: AttestToken ->
            AuthRequest(nonceScope.projectId, nonceScope.userId, AuthRequestBody(encryptedProjectSecret, googleAttestation.value))
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

    private fun Completable.fetchProjectRemoteConfigSettings(projectId: String): Single<JSONObject> =
        andThen(
            dbManager.remote.loadProjectRemoteConfigSettingsJsonString(projectId)
        )

    private fun Single<out JSONObject>.storeProjectRemoteConfigSettings(): Completable =
        flatMapCompletable {
            remoteConfigWrapper.projectSettingsJsonString = it.toString()
            Completable.complete()
        }
}
