package com.simprints.id.data.db.common

import com.google.firebase.FirebaseApp
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.secure.models.Token

interface RemoteDbManager {

    suspend fun signIn(token: Token)
    fun signOut()

    /** @throws DifferentProjectIdSignedInException */
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

}
