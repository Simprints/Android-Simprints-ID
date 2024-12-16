package com.simprints.infra.authlogic.authenticator

import com.simprints.fingerprint.infra.scanner.data.FirmwareRepository
import com.simprints.infra.authlogic.authenticator.remote.AuthenticationRemoteDataSource
import com.simprints.infra.authlogic.integrity.IntegrityTokenRequester
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.authlogic.model.NonceScope
import com.simprints.infra.authstore.domain.models.AuthRequest
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.AuthRequestInvalidCredentialsException
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.flow.collect
import java.io.IOException
import javax.inject.Inject

internal class ProjectAuthenticator @Inject constructor(
    private val secureDataManager: SecurityManager,
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource,
    private val integrityTokenRequester: IntegrityTokenRequester,
    private val firmwareRepository: FirmwareRepository,
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
    ) {
        createLocalDbKeyForProject(nonceScope.projectId)

        makeAuthRequest(prepareAuthRequestParameters(nonceScope, projectSecret), nonceScope)
            .signIn(nonceScope.projectId)

        val config = configManager.getProjectConfiguration()
        fetchProjectLongConsentTexts(config.general.languageOptions, config.projectId)

        // This is safe to call even on face-only projects as it will do nothing in such cases
        firmwareRepository.updateStoredFirmwareFilesWithLatest()
    }

    private suspend fun prepareAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String,
    ): AuthRequest = buildAuthRequestParameters(nonceScope, projectSecret)

    private suspend fun buildAuthRequestParameters(
        nonceScope: NonceScope,
        projectSecret: String,
    ): AuthRequest {
        val authenticationData = authenticationRemoteDataSource.requestAuthenticationData(
            nonceScope.projectId,
            nonceScope.deviceId,
        )
        return buildAuthRequest(
            projectSecret,
            integrityTokenRequester.getToken(authenticationData.nonce),
        )
    }

    private fun buildAuthRequest(
        projectSecret: String,
        integrityToken: String,
    ): AuthRequest = AuthRequest(projectSecret, integrityToken)

    private suspend fun makeAuthRequest(
        authRequest: AuthRequest,
        nonceScope: NonceScope,
    ): Token = authenticationRemoteDataSource.requestAuthToken(
        nonceScope.projectId,
        nonceScope.deviceId,
        authRequest,
    )

    private suspend fun Token.signIn(projectId: String) {
        signerManager.signIn(projectId, this)
    }

    private fun createLocalDbKeyForProject(projectId: String) {
        secureDataManager.createLocalDatabaseKeyIfMissing(projectId)
    }

    private suspend fun fetchProjectLongConsentTexts(
        languages: List<String>,
        projectId: String,
    ) {
        languages.forEach { language ->
            configManager.getPrivacyNotice(projectId, language).collect()
        }
    }
}
