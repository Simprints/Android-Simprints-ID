package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonElement
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.secure.models.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import java.io.IOException
import javax.inject.Inject

open class ProjectAuthenticator(component: AppComponent,
                                private val safetyNetClient: SafetyNetClient,
                                secureApiClient: SecureApiInterface,
                                private val attestationManager: AttestationManager = AttestationManager(),
                                private val authenticationDataManager:AuthenticationDataManager = AuthenticationDataManager(secureApiClient)) {

    @Inject lateinit var secureDataManager: SecureDataManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var remoteConfigWrapper: RemoteConfigWrapper
    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var preferencesManager: PreferencesManager

    private val projectSecretManager by lazy { ProjectSecretManager(loginInfoManager) }
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
        createLocalDbKeyForProject(nonceScope.projectId)
            .prepareAuthRequestParameters(nonceScope, projectSecret)
            .makeAuthRequest()
            .signIn(nonceScope.projectId, nonceScope.userId)
            .fetchProjectRemoteConfigSettings(nonceScope.projectId)
            .storeProjectRemoteConfigSettingsAndReturnProjectLanguages()
            .fetchProjectLongConsentTexts()

    private fun Completable.prepareAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): Single<AuthRequest> =
        andThen(buildAuthRequestParameters(nonceScope, projectSecret))

    private fun buildAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): Single<AuthRequest> {
        val authenticationData = getAuthenticationData(nonceScope.projectId, nonceScope.userId)
        val encryptedProjectSecret = getEncryptedProjectSecret(projectSecret, authenticationData)
        val googleAttestation = getGoogleAttestation(safetyNetClient, authenticationData)

        return zipAuthRequestParameters(encryptedProjectSecret, googleAttestation, nonceScope)
    }

    internal fun getAuthenticationData(projectId: String, userId: String) =
        authenticationDataManager.requestAuthenticationData(projectId, userId)

    private fun getEncryptedProjectSecret(projectSecret: String, authenticationDataSingle: Single<AuthenticationData>): Single<String> =
        authenticationDataSingle.flatMap {
            Single.just(projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, it.publicKeyString))
        }

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, authenticationDataSingle: Single<AuthenticationData>): Single<AttestToken> =
        authenticationDataSingle.flatMap {
            attestationManager.requestAttestation(safetyNetClient, it.nonce)
        }

    private fun zipAuthRequestParameters(encryptedProjectSecretSingle: Single<String>,
                                         googleAttestationSingle: Single<AttestToken>,
                                         nonceScope: NonceScope): Single<AuthRequest> =
        Singles.zip(encryptedProjectSecretSingle, googleAttestationSingle) {
            encryptedProjectSecret: String, googleAttestation: AttestToken ->
            AuthRequest(nonceScope.projectId, nonceScope.userId, AuthRequestBody(encryptedProjectSecret, googleAttestation.value))
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Token> =
        flatMap { authRequest ->
            authManager.requestAuthToken(authRequest)
        }

    private fun Single<out Token>.signIn(projectId: String, userId: String): Completable =
        flatMapCompletable { tokens ->
            dbManager.signIn(projectId, userId, tokens)
        }

    private fun createLocalDbKeyForProject(projectId: String) = Completable.fromAction {
        secureDataManager.setLocalDatabaseKey(projectId)
    }

    private fun Completable.fetchProjectRemoteConfigSettings(projectId: String): Single<JsonElement> =
        andThen(
            dbManager.remoteProjectManager.loadProjectRemoteConfigSettingsJsonString(projectId)
        )

    private fun Single<out JsonElement>.storeProjectRemoteConfigSettingsAndReturnProjectLanguages(): Single<Array<String>> =
        flatMap {
            remoteConfigWrapper.projectSettingsJsonString = it.toString()
            Single.just(preferencesManager.projectLanguages)
        }

    private fun Single<out Array<String>>.fetchProjectLongConsentTexts(): Completable =
        flatMapCompletable { languages ->
            longConsentManager.downloadAllLongConsents(languages)
        }
}
