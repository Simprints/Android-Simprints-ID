package com.simprints.id.secure

import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.secure.models.NonceScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class ProjectAuthenticatorImpl @Inject constructor(
    private val loginManager: LoginManager,
    private val projectSecretManager: ProjectSecretManager,
    private val secureDataManager: SecurityManager,
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val longConsentRepository: LongConsentRepository,
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

        val config = configManager.refreshProjectConfiguration(nonceScope.projectId)

        config.general.languageOptions.fetchProjectLongConsentTexts()
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
        val authenticationData = loginManager.requestAuthenticationData(
            nonceScope.projectId,
            nonceScope.userId,
            deviceId
        )
        return buildAuthRequest(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            loginManager.requestAttestation(authenticationData.nonce),
            deviceId
        )
    }

    private fun getEncryptedProjectSecret(
        projectSecret: String,
        authenticationData: AuthenticationData
    ): String =
        projectSecretManager.encryptAndStoreAndReturnProjectSecret(
            projectSecret,
            authenticationData.publicKey
        )

    private fun buildAuthRequest(
        encryptedProjectSecret: String,
        playIntegrotyToken: String,
        deviceId: String
    ): AuthRequest = AuthRequest(encryptedProjectSecret, playIntegrotyToken, deviceId)


    private suspend fun AuthRequest.makeAuthRequest(nonceScope: NonceScope): Token =
        loginManager.requestAuthToken(
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

    private suspend fun List<String>.fetchProjectLongConsentTexts() {
        longConsentRepository.deleteLongConsents()
        forEach { language ->
            longConsentRepository.getLongConsentResultForLanguage(language).catch { Simber.e(it) }
                .collect()
        }
    }
}
