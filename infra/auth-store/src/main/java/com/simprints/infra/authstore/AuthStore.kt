package com.simprints.infra.authstore

import com.google.firebase.FirebaseApp
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import kotlin.reflect.KClass

interface AuthStore {

    var signedInProjectId: String

    fun isProjectIdSignedIn(possibleProjectId: String): Boolean
    fun cleanCredentials()
    fun storeCredentials(projectId: String)

    suspend fun storeFirebaseToken(token: Token)
    fun clearFirebaseToken()

    fun isSignedIn(projectId: String): Boolean

    /**
     * Get the FirebaseApp that corresponds with the core backend. This FirebaseApp is only
     * initialized once the client has logged in.
     * @see storeFirebaseToken
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
}
