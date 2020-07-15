package com.simprints.face.license.data.repository

import kotlinx.coroutines.flow.Flow

interface LicenseRepository {
    suspend fun getLicense(projectId: String, deviceId: String): String?
    fun getLicenseStateFlow(projectId: String, deviceId: String): Flow<LicenseState>
}
