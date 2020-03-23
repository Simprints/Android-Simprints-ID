package com.simprints.id.secure

import androidx.annotation.VisibleForTesting
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonElement
import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.id.data.consent.longconsent.LongConsentRepository
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
import io.reactivex.schedulers.Schedulers
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
    private val longConsentRepository: LongConsentRepository,
    private val preferencesManager: PreferencesManager,
    private val attestationManager: AttestationManager = AttestationManager(),
    private val authenticationDataManager: AuthenticationDataManager = AuthenticationDataManager(secureApiClient)
) : ProjectAuthenticator {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val projectSecretManager by lazy { ProjectSecretManager(loginInfoManager) }

    private val authManager = AuthManager(secureApiClient)

    override suspend fun authenticate(nonceScope: NonceScope, projectSecret: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            createLocalDbKeyForProject(nonceScope.projectId)
                .prepareAuthRequestParameters(nonceScope, projectSecret)
                .makeAuthRequest()
                .signIn(nonceScope.projectId, nonceScope.userId)
                .fetchProjectRemoteConfigSettings(nonceScope.projectId)
                .storeProjectRemoteConfigSettingsAndReturnProjectLanguages()
                .fetchProjectLongConsentTexts()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onComplete = {
                        println("TEST_ALAN - onComplete")
                        continuation.resumeSafely(Unit)
                    },
                    onError = {
                        Timber.e(it)
                        println("TEST_ALAN - onError")
                        continuation.resumeWithExceptionSafely(it)
                    }
                )
        }
    }

    private fun Completable.prepareAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String
    ): Single<AuthRequest> {
        println("TEST_ALAN - prepareAuthRequestParameters")
        return andThen(buildAuthRequestParameters(nonceScope, projectSecret))
    }

    private fun buildAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String
    ): Single<AuthRequest> {
        return getAuthenticationData(
            nonceScope.projectId,
            nonceScope.userId
        ).flatMap { authenticationData ->
            println("TEST_ALAN - buildAuthRequestParameters")
            zipAuthRequestParameters(
                getEncryptedProjectSecret(projectSecret, authenticationData),
                getGoogleAttestation(safetyNetClient, authenticationData),
                nonceScope
            )
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Token> {
        return flatMap { authRequest ->
            println("TEST_ALAN - makeAuthRequest")
            authManager.requestAuthToken(authRequest)
        }
    }

    private fun Single<out Token>.signIn(projectId: String, userId: String): Completable {
        return flatMapCompletable { tokens ->
            println("TEST_ALAN - signIn")
            signerManager.signIn(projectId, userId, tokens)
        }
    }

    private fun createLocalDbKeyForProject(projectId: String) = Completable.fromAction {
        secureDataManager.setLocalDatabaseKey(projectId)
    }

    private fun Completable.fetchProjectRemoteConfigSettings(
        projectId: String
    ): Single<JsonElement> {
        return andThen(singleWithSuspend {
            println("TEST_ALAN - fetchProjectRemoteConfigSettings")
            projectRemoteDataSource.loadProjectRemoteConfigSettingsJsonString(
                projectId
            )
        })
    }

    private fun Single<out JsonElement>.storeProjectRemoteConfigSettingsAndReturnProjectLanguages(
    ): Single<Array<String>> {
        return flatMap {
            println("TEST_ALAN - storeProjectRemoteConfigSettingsAndReturnProjectLanguages")
            remoteConfigWrapper.projectSettingsJsonString = it.toString()
            Single.just(preferencesManager.projectLanguages)
        }
    }

    private fun Single<out Array<String>>.fetchProjectLongConsentTexts(): Completable {
        return flatMapCompletable { languages ->
            println("TEST_ALAN - fetchProjectLongConsentTexts")
            completableWithSuspend { longConsentRepository.downloadLongConsentForLanguages(languages) }
        }
    }
}
