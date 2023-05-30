package com.simprints.id.secure

import com.simprints.id.secure.models.NonceScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.AuthRequest
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class ProjectAuthenticatorImpl @Inject constructor(
    private val authStore: AuthStore,
    private val projectSecretManager: ProjectSecretManager,
    private val secureDataManager: SecurityManager,
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
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

        config.general.languageOptions.fetchProjectLongConsentTexts(nonceScope.projectId)
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
        val authenticationData = authStore.requestAuthenticationData(
            nonceScope.projectId,
            nonceScope.userId,
            deviceId
        )
        return buildAuthRequest(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            authStore.requestIntegrityToken(authenticationData.nonce),
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
        integrityToken: String,
        deviceId: String
    ): AuthRequest = AuthRequest(encryptedProjectSecret, integrityToken, deviceId)


    private suspend fun AuthRequest.makeAuthRequest(nonceScope: NonceScope): Token =
        authStore.requestAuthToken(
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

    private suspend fun List<String>.fetchProjectLongConsentTexts(projectId: String) {
        forEach { language ->
            configManager.getPrivacyNotice(projectId, language).collect()
        }
    }
}
