package com.simprints.id.secure

import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.secure.models.NonceScope
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.domain.AttestationManager
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource
import com.simprints.infra.security.cryptography.ProjectSecretEncrypter
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class ProjectAuthenticatorImpl(
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource,
    private val secureDataManager: SecureLocalDbKeyProvider,
    private val projectRepository: ProjectRepository,
    private val signerManager: SignerManager,
    private val longConsentRepository: LongConsentRepository,
    private val preferencesManager: IdPreferencesManager,
    private val attestationManager: AttestationManager,
) : ProjectAuthenticator {

    override suspend fun authenticate(
        nonceScope: NonceScope,
        projectSecret: String,
        deviceId: String
    ) {
        createLocalDbKeyForProject(nonceScope.projectId)

        prepareAuthRequestParameters(nonceScope, projectSecret, deviceId)
            .makeAuthRequest(nonceScope)
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
        val authenticationData = authenticationRemoteDataSource.requestAuthenticationData(
            nonceScope.projectId,
            nonceScope.userId,
            deviceId
        )
        return buildAuthRequest(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            attestationManager.requestAttestation(authenticationData.nonce),
            deviceId
        )
    }

    private fun getEncryptedProjectSecret(
        projectSecret: String,
        authenticationData: AuthenticationData
    ): String =
        ProjectSecretEncrypter(authenticationData.publicKey).encrypt(projectSecret)

    private fun buildAuthRequest(
        encryptedProjectSecret: String,
        googleAttestation: String,
        deviceId: String
    ): AuthRequest = AuthRequest(encryptedProjectSecret, googleAttestation, deviceId)


    private suspend fun AuthRequest.makeAuthRequest(nonceScope: NonceScope): Token =
        authenticationRemoteDataSource.requestAuthToken(
            nonceScope.projectId,
            nonceScope.userId,
            this
        )

    private suspend fun Token.signIn(projectId: String, userId: String) {
        signerManager.signIn(projectId, userId, this)
    }

    private fun createLocalDbKeyForProject(projectId: String) {
        secureDataManager.createLocalDatabaseKeyIfMissing(projectId)
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
        forEach { language ->
            longConsentRepository.getLongConsentResultForLanguage(language).catch { Simber.e(it) }
                .collect()
        }
    }
}
