package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.secure.models.*
import com.simprints.logging.Simber
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class ProjectAuthenticatorImpl(
    private val authManager: AuthManager,
    private val projectSecretManager: ProjectSecretManager,
    private val safetyNetClient: SafetyNetClient,
    private val secureDataManager: SecureLocalDbKeyProvider,
    private val projectRepository: ProjectRepository,
    private val signerManager: SignerManager,
    private val longConsentRepository: LongConsentRepository,
    private val preferencesManager: IdPreferencesManager,
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

        projectRepository.fetchProjectConfigurationAndSave(nonceScope.projectId)

        updateLanguageAndReturnProjectLanguages().fetchProjectLongConsentTexts()
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

    private fun getEncryptedProjectSecret(
        projectSecret: String,
        authenticationData: AuthenticationData
    ): String =
        projectSecretManager.encryptAndStoreAndReturnProjectSecret(
            projectSecret,
            authenticationData.publicKeyString
        )

    private fun getGoogleAttestation(
        safetyNetClient: SafetyNetClient,
        authenticationData: AuthenticationData
    ): AttestToken =
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

    private fun updateLanguageAndReturnProjectLanguages(): Array<String> {
        /*We need to override the language in the helper as the language context is initialised in Application
         in attachBaseContext() which is  called before initialising dagger component.
         Thus we cannot use preferences manager to get the language.*/
        LanguageHelper.language = preferencesManager.language

        return preferencesManager.projectLanguages
    }

    private suspend fun Array<String>.fetchProjectLongConsentTexts() {
        longConsentRepository.deleteLongConsents()
        forEach {
            longConsentRepository.getLongConsentResultForLanguage(it).catch { Simber.e(it) }
                .collect()
        }
    }
}
