package com.simprints.infra.login

import com.simprints.infra.login.domain.AttestationManager
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource


internal class LoginManagerImpl(
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource,
    private val attestationManager: AttestationManager,
    private val loginInfoManager: LoginInfoManager,
) : LoginManager {

    override var projectIdTokenClaim: String?
        get() = loginInfoManager.projectIdTokenClaim
        set(value) {
            loginInfoManager.projectIdTokenClaim = value
        }
    override var userIdTokenClaim: String?
        get() = loginInfoManager.userIdTokenClaim
        set(value) {
            loginInfoManager.userIdTokenClaim = value
        }
    override var encryptedProjectSecret: String
        get() = loginInfoManager.encryptedProjectSecret
        set(value) {
            loginInfoManager.encryptedProjectSecret = value
        }
    override var signedInProjectId: String
        get() = loginInfoManager.signedInProjectId
        set(value) {
            loginInfoManager.signedInProjectId = value
        }
    override var signedInUserId: String
        get() = loginInfoManager.signedInUserId
        set(value) {
            loginInfoManager.signedInUserId = value
        }
    override var coreFirebaseProjectId: String
        get() = loginInfoManager.coreFirebaseProjectId
        set(value) {
            loginInfoManager.coreFirebaseProjectId = value
        }
    override var coreFirebaseApplicationId: String
        get() = loginInfoManager.coreFirebaseApplicationId
        set(value) {
            loginInfoManager.coreFirebaseApplicationId = value
        }
    override var coreFirebaseApiKey: String
        get() = loginInfoManager.coreFirebaseApiKey
        set(value) {
            loginInfoManager.coreFirebaseApiKey = value
        }

    override fun requestAttestation(nonce: String): String =
        attestationManager.requestAttestation(nonce)

    override suspend fun requestAuthenticationData(
        projectId: String,
        userId: String,
        deviceId: String
    ): AuthenticationData =
        authenticationRemoteDataSource.requestAuthenticationData(projectId, userId, deviceId)

    override suspend fun requestAuthToken(
        projectId: String,
        userId: String,
        credentials: AuthRequest
    ): Token = authenticationRemoteDataSource.requestAuthToken(projectId, userId, credentials)

    override fun getEncryptedProjectSecretOrEmpty(): String =
        loginInfoManager.getEncryptedProjectSecretOrEmpty()

    override fun getSignedInProjectIdOrEmpty(): String =
        loginInfoManager.getSignedInProjectIdOrEmpty()

    override fun getSignedInUserIdOrEmpty(): String =
        loginInfoManager.getSignedInUserIdOrEmpty()

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        loginInfoManager.isProjectIdSignedIn(possibleProjectId)

    override fun cleanCredentials() {
        loginInfoManager.cleanCredentials()
    }

    override fun clearCachedTokenClaims() {
        loginInfoManager.clearCachedTokenClaims()
    }

    override fun storeCredentials(projectId: String, userId: String) {
        loginInfoManager.storeCredentials(projectId, userId)
    }
}
