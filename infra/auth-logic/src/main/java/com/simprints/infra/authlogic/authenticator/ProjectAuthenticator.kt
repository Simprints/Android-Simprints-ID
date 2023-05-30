package com.simprints.infra.authlogic.authenticator

import com.simprints.infra.authlogic.integrity.IntegrityTokenRequester
import com.simprints.infra.authlogic.authenticator.remote.AuthenticationRemoteDataSource
import com.simprints.infra.authlogic.model.NonceScope
import com.simprints.infra.authstore.domain.models.AuthRequest
import com.simprints.infra.authstore.domain.models.AuthenticationData
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.flow.collect
import java.io.IOException
import javax.inject.Inject

internal class ProjectAuthenticator @Inject constructor(
    private val projectSecretManager: ProjectSecretManager,
    private val secureDataManager: SecurityManager,
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource,
    private val integrityTokenRequester: IntegrityTokenRequester,
) {

    /**
     * @throws IOException
     * @throws AuthRequestInvalidCredentialsException
     * @throws BackendMaintenanceException
     * @throws SyncCloudIntegrationException
     * @throws RequestingIntegrityTokenException
     */
    suspend fun authenticate(
        nonceScope: NonceScope,
        projectSecret: String,
        deviceId: String
    ) {
        createLocalDbKeyForProject(nonceScope.projectId)

        makeAuthRequest(prepareAuthRequestParameters(nonceScope, projectSecret, deviceId), nonceScope)
            .signIn(nonceScope.projectId, nonceScope.userId)

        val config = configManager.refreshProjectConfiguration(nonceScope.projectId)

        fetchProjectLongConsentTexts(config.general.languageOptions, nonceScope.projectId)
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
            integrityTokenRequester.getToken(authenticationData.nonce),
            deviceId
        )
    }

    private fun getEncryptedProjectSecret(
        projectSecret: String,
        authenticationData: AuthenticationData
    ): String = projectSecretManager.encryptProjectSecret(
        projectSecret,
        authenticationData.publicKey
    )

    private fun buildAuthRequest(
        encryptedProjectSecret: String,
        integrityToken: String,
        deviceId: String
    ): AuthRequest = AuthRequest(encryptedProjectSecret, integrityToken, deviceId)


    private suspend fun makeAuthRequest(authRequest: AuthRequest, nonceScope: NonceScope): Token =
        authenticationRemoteDataSource.requestAuthToken(
            nonceScope.projectId,
            nonceScope.userId,
            authRequest
        )

    private suspend fun Token.signIn(projectId: String, userId: String) {
        signerManager.signIn(projectId, userId, this)
    }

    private fun createLocalDbKeyForProject(projectId: String) {
        secureDataManager.createLocalDatabaseKeyIfMissing(projectId)
    }

    private suspend fun fetchProjectLongConsentTexts(languages: List<String>, projectId: String) {
        languages.forEach { language ->
            configManager.getPrivacyNotice(projectId, language).collect()
        }
    }
}
