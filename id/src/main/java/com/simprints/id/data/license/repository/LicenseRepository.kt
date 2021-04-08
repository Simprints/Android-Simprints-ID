package com.simprints.id.data.license.repository

import kotlinx.coroutines.flow.Flow

interface LicenseRepository {
    fun getLicenseStates(projectId: String, deviceId: String, licenseVendor: LicenseVendor): Flow<LicenseState>
    fun deleteCachedLicense()
}
