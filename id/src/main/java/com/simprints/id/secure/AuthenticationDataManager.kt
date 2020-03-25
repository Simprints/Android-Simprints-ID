package com.simprints.id.secure

import com.simprints.id.secure.models.AuthenticationData

interface AuthenticationDataManager {
    suspend fun requestAuthenticationData(projectId: String, userId: String): AuthenticationData
}
