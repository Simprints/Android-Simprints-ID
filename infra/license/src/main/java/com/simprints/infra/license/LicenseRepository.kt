package com.simprints.infra.license

import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseState
import com.simprints.infra.license.models.Vendor
import kotlinx.coroutines.flow.Flow

interface LicenseRepository {

    fun getLicenseStates(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor,
    ): Flow<LicenseState>

    suspend fun getCachedLicense(licenseVendor: Vendor): License?
    suspend fun deleteCachedLicense(licenseVendor: Vendor)
    suspend fun deleteCachedLicenses()
}
