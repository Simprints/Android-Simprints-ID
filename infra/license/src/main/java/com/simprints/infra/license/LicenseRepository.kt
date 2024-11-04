package com.simprints.infra.license

import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseState
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import kotlinx.coroutines.flow.Flow

interface LicenseRepository {

    fun redownloadLicence(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor,
        requiredVersion: LicenseVersion,
    ): Flow<LicenseState>

    fun getLicenseStates(
        projectId: String,
        deviceId: String,
        licenseVendor: Vendor,
        version: LicenseVersion,
    ): Flow<LicenseState>

    suspend fun getCachedLicense(licenseVendor: Vendor): License?
    suspend fun deleteCachedLicense(licenseVendor: Vendor)
    suspend fun deleteCachedLicenses()
}
