package com.simprints.face.license.data.repository

import kotlinx.coroutines.flow.Flow

interface LicenseRepository {
    fun getLicenseStates(projectId: String, deviceId: String): Flow<LicenseState>
    fun deleteCachedLicense()
}
