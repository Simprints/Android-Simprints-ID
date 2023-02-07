package com.simprints.infra.license

import kotlinx.coroutines.flow.Flow

interface LicenseRepository {

    fun getLicenseStates(projectId: String, deviceId: String, licenseVendor: LicenseVendor): Flow<LicenseState>

    suspend fun deleteCachedLicense()

}
