package com.simprints.id.secure

import com.simprints.infra.login.domain.models.Token

interface SignerManager {

    suspend fun signIn(projectId: String, userId: String, token: Token)
    suspend fun signOut()
}
