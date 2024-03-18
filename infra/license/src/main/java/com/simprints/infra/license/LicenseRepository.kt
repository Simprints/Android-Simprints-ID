package com.simprints.infra.license

import kotlinx.coroutines.flow.Flow

interface LicenseRepository {

    fun getLicenseStates(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor,
    ): Flow<LicenseState>

    suspend fun getCachedLicense(licenseVendor: Vendor): String
    suspend fun deleteCachedLicense(licenseVendor: Vendor)

}
