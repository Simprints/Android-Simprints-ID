package com.simprints.infra.login

import com.google.firebase.FirebaseApp
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

interface LoginManager {
    suspend fun requestIntegrityToken(nonce: String): String

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

    suspend fun signIn(token: Token)
    fun signOut()

    fun isSignedIn(projectId: String, userId: String): Boolean

    suspend fun getCurrentToken(): String

    /**
     * Get the FirebaseApp that corresponds with the core backend. This FirebaseApp is only
     * initialized once the client has logged in.
     * @see signIn
     * @return FirebaseApp
     * @throws IllegalStateException if not initialized
     */
    fun getCoreApp(): FirebaseApp

    @Deprecated(
        message = "Since 2021.2.0. Can be removed once all projects are on 2021.2.0+",
        replaceWith = ReplaceWith("getCoreApp()")
    )
    fun getLegacyAppFallback(): FirebaseApp

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T>
    fun <T : SimRemoteInterface> buildUnauthenticatedClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T>
}
