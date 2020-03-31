package com.simprints.id.secure

import com.simprints.id.secure.models.Token

interface SignerManager {

    suspend fun signIn(projectId: String, userId: String, token: Token)
    suspend fun signOut()
}
