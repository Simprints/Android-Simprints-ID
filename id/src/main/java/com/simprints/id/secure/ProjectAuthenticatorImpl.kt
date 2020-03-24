package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonElement
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.secure.models.*

class ProjectAuthenticatorImpl(
    secureApiClient: SecureApiInterface,
    private val projectSecretManager: ProjectSecretManager,
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

    private val authManager = AuthManager(secureApiClient)

    override suspend fun authenticate(nonceScope: NonceScope, projectSecret: String) {
        println("ProjectAuthenticator: Authenticate")
        createLocalDbKeyForProject(nonceScope.projectId)

        prepareAuthRequestParameters(nonceScope, projectSecret)
            .makeAuthRequest()
            .signIn(nonceScope.projectId, nonceScope.userId)

        fetchProjectRemoteConfigSettings(nonceScope.projectId)
            .storeProjectRemoteConfigSettingsAndReturnProjectLanguages()
            .fetchProjectLongConsentTexts()
    }

    private suspend fun prepareAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): AuthRequest = try {
        buildAuthRequestParameters(nonceScope, projectSecret)
    } catch (t: Throwable) {
        println("ProjectAuthenticator: $t")
        throw t
    }

    private suspend fun buildAuthRequestParameters(nonceScope: NonceScope, projectSecret: String): AuthRequest {
        val authenticationData = getAuthenticationData(nonceScope.projectId, nonceScope.userId)
        return buildAuthRequest(
            getEncryptedProjectSecret(projectSecret, authenticationData),
            getGoogleAttestation(safetyNetClient, authenticationData),
            nonceScope)
    }

    internal suspend fun getAuthenticationData(projectId: String, userId: String) = try {
        authenticationDataManager.requestAuthenticationData(projectId, userId)
    }  catch (t: Throwable) {
        println("ProjectAuthenticator: $t")
        throw t
    }

    private fun getEncryptedProjectSecret(projectSecret: String, authenticationData: AuthenticationData): String = try {
        projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret,
            authenticationData.publicKeyString)
    } catch (t: Throwable) {
        println("ProjectAuthenticator: $t")
        throw t
    }

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, authenticationData: AuthenticationData): AttestToken = try {
        attestationManager.requestAttestation(safetyNetClient, authenticationData.nonce)
    } catch (t: Throwable) {
        println("ProjectAuthenticator: $t")
        throw t
    }

    private fun buildAuthRequest(encryptedProjectSecret: String,
                                 googleAttestation: AttestToken,
                                 nonceScope: NonceScope): AuthRequest =
        AuthRequest(nonceScope.projectId, nonceScope.userId, AuthRequestBody(encryptedProjectSecret, googleAttestation.value))

    private suspend fun AuthRequest.makeAuthRequest(): Token = try {
        authManager.requestAuthToken(this)
    } catch (t: Throwable) {
        println("ProjectAuthenticator: $t")
        throw t
    }

    private suspend fun Token.signIn(projectId: String, userId: String) {
        try {
            signerManager.signIn(projectId, userId, this)
        } catch (t: Throwable) {
            println("ProjectAuthenticator: $t")
            throw t
        }
    }

    private fun createLocalDbKeyForProject(projectId: String) {
        secureDataManager.setLocalDatabaseKey(projectId)
    }

    private suspend fun fetchProjectRemoteConfigSettings(projectId: String): JsonElement = try {
        projectRemoteDataSource.loadProjectRemoteConfigSettingsJsonString(projectId)
    } catch (t: Throwable) {
        println("ProjectAuthenticator: $t")
        throw t
    }

    private fun JsonElement.storeProjectRemoteConfigSettingsAndReturnProjectLanguages(): Array<String> {
        remoteConfigWrapper.projectSettingsJsonString = this.toString()
        return preferencesManager.projectLanguages
    }

    private suspend fun Array<String>.fetchProjectLongConsentTexts() =
        longConsentRepository.downloadLongConsentForLanguages(this)
}
