package com.simprints.infra.authstore

import com.google.firebase.FirebaseApp
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface AuthStore {
    var signedInUserId: TokenizableString?
    var signedInProjectId: String

    fun isProjectIdSignedIn(possibleProjectId: String): Boolean

    fun observeSignedInProjectId(): Flow<String>

    fun cleanCredentials()

    suspend fun storeFirebaseToken(token: Token)

    fun clearFirebaseToken()

    fun isFirebaseSignedIn(projectId: String): Boolean

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
        replaceWith = ReplaceWith("getCoreApp()"),
    )
    fun getLegacyAppFallback(): FirebaseApp

    suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T>
}
