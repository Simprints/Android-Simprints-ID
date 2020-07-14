package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonElement
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.models.*
import com.simprints.core.tools.utils.LanguageHelper

class ProjectAuthenticatorImpl(
    private val authManager: AuthManager,
    private val projectSecretManager: ProjectSecretManager,
    private val safetyNetClient: SafetyNetClient,
    private val secureDataManager: SecureLocalDbKeyProvider,
    private val projectRemoteDataSource: ProjectRemoteDataSource,
    private val signerManager: SignerManager,
    private val remoteConfigWrapper: RemoteConfigWrapper,
    private val longConsentRepository: LongConsentRepository,
    private val preferencesManager: PreferencesManager,
    private val attestationManager: AttestationManager,
    private val authenticationDataManager: AuthenticationDataManager
) : ProjectAuthenticator {

    override suspend fun authenticate(
        nonceScope: NonceScope,
        projectSecret: String,
        deviceId: String
    ) {
        createLocalDbKeyForProject(nonceScope.projectId)

        prepareAuthRequestParameters(nonceScope, projectSecret, deviceId)
            .makeAuthRequest()
            .signIn(nonceScope.projectId, nonceScope.userId)

        fetchProjectRemoteConfigSettings(nonceScope.projectId)
            .storeProjectRemoteConfigSettingsUpdateLanguageAndReturnProjectLanguages()
            .fetchProjectLongConsentTexts()
    }

    private suspend fun prepareAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String,
        deviceId: String
    ): AuthRequest = buildAuthRequestParameters(nonceScope, projectSecret, deviceId)

    private suspend fun buildAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String,
        deviceId: String
    ): AuthRequest {
        val authenticationData = getAuthenticationData(nonceScope.projectId, nonceScope.userId)
        return buildAuthRequest(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            getGoogleAttestation(safetyNetClient, authenticationData),
            nonceScope,
            deviceId
        )
    }

    private suspend fun getAuthenticationData(projectId: String, userId: String) =
        authenticationDataManager.requestAuthenticationData(projectId, userId)

    private fun getEncryptedProjectSecret(projectSecret: String, authenticationData: AuthenticationData): String =
        projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret,
            authenticationData.publicKeyString)

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, authenticationData: AuthenticationData): AttestToken =
        attestationManager.requestAttestation(safetyNetClient, authenticationData.nonce)

    private fun buildAuthRequest(
        encryptedProjectSecret: String,
        googleAttestation: AttestToken,
        nonceScope: NonceScope,
        deviceId: String
    ): AuthRequest = AuthRequest(
        nonceScope.projectId,
        nonceScope.userId,
        AuthRequestBody(encryptedProjectSecret, googleAttestation.value, deviceId)
    )

    private suspend fun AuthRequest.makeAuthRequest(): Token =
        authManager.requestAuthToken(this)

    private suspend fun Token.signIn(projectId: String, userId: String) {
        signerManager.signIn(projectId, userId, this)
    }

    private fun createLocalDbKeyForProject(projectId: String) {
        secureDataManager.setLocalDatabaseKey(projectId)
    }

    private suspend fun fetchProjectRemoteConfigSettings(projectId: String): JsonElement =
        projectRemoteDataSource.loadProjectRemoteConfigSettingsJsonString(projectId)

    private fun JsonElement.storeProjectRemoteConfigSettingsUpdateLanguageAndReturnProjectLanguages(): Array<String> {
        val jsonString = this.toString()
        remoteConfigWrapper.projectSettingsJsonString = jsonString

        /*We need to override the language in the helper as the language context is initialised in Application
         in attachBaseContext() which is  called before initialising dagger component.
         Thus we cannot use preferences manager to get the language.*/
        LanguageHelper.language = preferencesManager.language

        return preferencesManager.projectLanguages
    }

    private suspend fun Array<String>.fetchProjectLongConsentTexts() {
        longConsentRepository.downloadLongConsentForLanguages(this)
    }
}
