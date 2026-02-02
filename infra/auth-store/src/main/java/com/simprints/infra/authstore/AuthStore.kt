package com.simprints.infra.authstore

import com.google.firebase.FirebaseApp
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.domain.models.Token
import kotlinx.coroutines.flow.Flow

interface AuthStore {
    var signedInUserId: TokenizableString?
    var signedInProjectId: String

    fun isProjectIdSignedIn(possibleProjectId: String): Boolean

    fun observeSignedInProjectId(): Flow<String>

    fun cleanCredentials()

    suspend fun storeFirebaseToken(token: Token)

    suspend fun getFirebaseToken(): String

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
}
