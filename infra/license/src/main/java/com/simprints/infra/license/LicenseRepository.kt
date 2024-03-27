package com.simprints.infra.license

import com.simprints.infra.license.remote.License
import kotlinx.coroutines.flow.Flow

interface LicenseRepository {

    fun getLicenseStates(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor,
    ): Flow<LicenseState>

    suspend fun getCachedLicense(licenseVendor: Vendor): License?
    suspend fun deleteCachedLicense(licenseVendor: Vendor)

}
