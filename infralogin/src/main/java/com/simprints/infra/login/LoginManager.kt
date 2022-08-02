package com.simprints.infra.login

import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token

interface LoginManager {
    fun requestAttestation(nonce: String): String

    suspend fun requestAuthenticationData(
        projectId: String,
        userId: String,
        deviceId: String
    ): AuthenticationData

    suspend fun requestAuthToken(
        projectId: String,
        userId: String,
        credentials: AuthRequest
    ): Token

    // Cached claims in the auth token. We used them to check whether the user is signed or not
    // in without reading the token from Firebase (async operation)
    var projectIdTokenClaim: String?
    var userIdTokenClaim: String?

    var encryptedProjectSecret: String
    var signedInProjectId: String
    var signedInUserId: String

    // Core Firebase Project details. We store them to initialize the core Firebase project.
    var coreFirebaseProjectId: String
    var coreFirebaseApplicationId: String
    var coreFirebaseApiKey: String

    fun getEncryptedProjectSecretOrEmpty(): String
    fun getSignedInProjectIdOrEmpty(): String

    fun getSignedInUserIdOrEmpty(): String
    fun isProjectIdSignedIn(possibleProjectId: String): Boolean
    fun cleanCredentials()
    fun clearCachedTokenClaims()
    fun storeCredentials(projectId: String, userId: String)
}
