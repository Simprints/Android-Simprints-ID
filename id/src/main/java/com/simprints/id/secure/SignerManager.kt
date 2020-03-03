package com.simprints.id.secure

import com.simprints.id.secure.models.Token
import io.reactivex.Completable

interface SignerManager {

    fun signIn(projectId: String, userId: String, token: Token): Completable
    suspend fun signOut()
}
