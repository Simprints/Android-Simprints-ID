package com.simprints.id.secure

import androidx.annotation.VisibleForTesting
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonElement
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.models.*

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
        createLocalDbKeyForProject(nonceScope.projectId)

        prepareAuthRequestParameters(nonceScope, projectSecret)
            .makeAuthRequest()
            .signIn(nonceScope.projectId, nonceScope.userId)

        fetchProjectRemoteConfigSettings(nonceScope.projectId)
            .storeProjectRemoteConfigSettingsAndReturnProjectLanguages()
            .fetchProjectLongConsentTexts()
    }

    private suspend fun prepareAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): AuthRequest =
        buildAuthRequestParameters(nonceScope, projectSecret)

    private suspend fun buildAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): AuthRequest {
        val authenticationData = getAuthenticationData(nonceScope.projectId, nonceScope.userId)
        return buildAuthRequest(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            getGoogleAttestation(safetyNetClient, authenticationData),
            nonceScope)
    }

    internal suspend fun getAuthenticationData(projectId: String, userId: String) =
        authenticationDataManager.requestAuthenticationData(projectId, userId)

    private fun getEncryptedProjectSecret(projectSecret: String, authenticationData: AuthenticationData): String =
        projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret,
            authenticationData.publicKeyString)

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, authenticationData: AuthenticationData): AttestToken =
        attestationManager.requestAttestation(safetyNetClient, authenticationData.nonce)

    private fun buildAuthRequest(encryptedProjectSecret: String,
                                 googleAttestation: AttestToken,
                                 nonceScope: NonceScope): AuthRequest =
        AuthRequest(nonceScope.projectId, nonceScope.userId, AuthRequestBody(encryptedProjectSecret, googleAttestation.value))

    private suspend fun AuthRequest.makeAuthRequest(): Token =
        authManager.requestAuthToken(this)

    private suspend fun Token.signIn(projectId: String, userId: String) {
        signerManager.signIn(projectId, userId, this)
    }

    private fun createLocalDbKeyForProject(projectId: String) = secureDataManager.setLocalDatabaseKey(projectId)

    private suspend fun fetchProjectRemoteConfigSettings(projectId: String): JsonElement =
        projectRemoteDataSource.loadProjectRemoteConfigSettingsJsonString(projectId)

    private fun JsonElement.storeProjectRemoteConfigSettingsAndReturnProjectLanguages(): Array<String> {
        remoteConfigWrapper.projectSettingsJsonString = this.toString()
        return preferencesManager.projectLanguages
    }

    private suspend fun Array<String>.fetchProjectLongConsentTexts() =
        longConsentRepository.downloadLongConsentForLanguages(this)
}
