package com.simprints.id.secure

import com.simprints.id.secure.models.NonceScope

interface ProjectAuthenticator {
    // TODO: rename
    suspend fun coAuthenticate(nonceScope: NonceScope, projectSecret: String)
}
