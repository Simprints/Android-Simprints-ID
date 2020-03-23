package com.simprints.id.secure

import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.secure.models.NonceScope
import java.io.IOException

interface ProjectAuthenticator {

    /**
     * @throws IOException
     * @throws AuthRequestInvalidCredentialsException
     * @throws SimprintsInternalServerException
     * @throws com.simprints.id.exceptions.safe.secure.SafetyNetException
     */
    suspend fun authenticate(nonceScope: NonceScope, projectSecret: String)
    suspend fun authenticate(nonceScope: NonceScope, projectSecret: String) {
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
