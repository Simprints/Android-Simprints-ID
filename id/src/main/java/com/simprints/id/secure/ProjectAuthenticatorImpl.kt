package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonElement
import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.models.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

class ProjectAuthenticatorImpl(
    secureApiClient: SecureApiInterface,
    loginInfoManager: LoginInfoManager,
    private val safetyNetClient: SafetyNetClient,
    private val secureDataManager: SecureLocalDbKeyProvider,
    private val projectRemoteDataSource: ProjectRemoteDataSource,
    private val signerManager: SignerManager,
    private val remoteConfigWrapper: RemoteConfigWrapper,
    private val longConsentManager: LongConsentManager,
    private val preferencesManager: PreferencesManager
) : ProjectAuthenticator {

    internal val projectSecretManager by lazy { ProjectSecretManager(loginInfoManager) }

    private val attestationManager = AttestationManager()
    private val authenticationDataManager = AuthenticationDataManager(secureApiClient)
    private val authManager = AuthManager(secureApiClient)

    override suspend fun authenticate(nonceScope: NonceScope, projectSecret: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                createLocalDbKeyForProject(nonceScope.projectId)
                    .prepareAuthRequestParameters(nonceScope, projectSecret)
                    .makeAuthRequest()
                    .signIn(nonceScope.projectId, nonceScope.userId)
                    .fetchProjectRemoteConfigSettings(nonceScope.projectId)
                    .storeProjectRemoteConfigSettingsAndReturnProjectLanguages()
                    .fetchProjectLongConsentTexts()
                    .subscribeBy(
                        onComplete = { continuation.resumeSafely(Unit) },
                        onError = {
                            Timber.e(it)
                            continuation.resumeWithExceptionSafely(it)
                        }
                    )
            }
        }
    }

    private fun Completable.prepareAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String
    ): Single<AuthRequest> =
        andThen(buildAuthRequestParameters(nonceScope, projectSecret))

    private fun buildAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String
    ): Single<AuthRequest> = getAuthenticationData(
        nonceScope.projectId,
        nonceScope.userId
    ).flatMap { authenticationData ->
        zipAuthRequestParameters(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            getGoogleAttestation(safetyNetClient, authenticationData),
            nonceScope
        )
    }

    internal fun getAuthenticationData(projectId: String, userId: String) =
        authenticationDataManager.requestAuthenticationData(projectId, userId)

    private fun getEncryptedProjectSecret(
        projectSecret: String,
        authenticationData: AuthenticationData
    ): Single<String> = Single.just(
        projectSecretManager.encryptAndStoreAndReturnProjectSecret(
            projectSecret,
            authenticationData.publicKeyString
        )
    )

    private fun getGoogleAttestation(
        safetyNetClient: SafetyNetClient,
        authenticationData: AuthenticationData
    ): Single<AttestToken> =
        attestationManager.requestAttestation(safetyNetClient, authenticationData.nonce)

    private fun zipAuthRequestParameters(
        encryptedProjectSecretSingle: Single<String>,
        googleAttestationSingle: Single<AttestToken>,
        nonceScope: NonceScope
    ): Single<AuthRequest> = Singles.zip(
        encryptedProjectSecretSingle,
        googleAttestationSingle
    ) { encryptedProjectSecret: String, googleAttestation: AttestToken ->
        AuthRequest(
            nonceScope.projectId,
            nonceScope.userId,
            AuthRequestBody(encryptedProjectSecret, googleAttestation.value)
        )
    }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Token> = flatMap { authRequest ->
        authManager.requestAuthToken(authRequest)
    }

    private fun Single<out Token>.signIn(projectId: String, userId: String): Completable =
        flatMapCompletable { tokens ->
            signerManager.signIn(projectId, userId, tokens)
        }

    private fun createLocalDbKeyForProject(projectId: String) = Completable.fromAction {
        secureDataManager.setLocalDatabaseKey(projectId)
    }

    private fun Completable.fetchProjectRemoteConfigSettings(
        projectId: String
    ): Single<JsonElement> = andThen(singleWithSuspend {
        projectRemoteDataSource.loadProjectRemoteConfigSettingsJsonString(
            projectId
        )
    })

    private fun Single<out JsonElement>.storeProjectRemoteConfigSettingsAndReturnProjectLanguages(
    ): Single<Array<String>> = flatMap {
        remoteConfigWrapper.projectSettingsJsonString = it.toString()
        Single.just(preferencesManager.projectLanguages)
    }

    private fun Single<out Array<String>>.fetchProjectLongConsentTexts(): Completable =
        flatMapCompletable { languages ->
            longConsentManager.downloadAllLongConsents(languages)
        }

}
