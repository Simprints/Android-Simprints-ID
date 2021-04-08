package com.simprints.id.data.license.remote

import com.simprints.id.data.license.repository.LicenseVendor

interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String, licenseVendor: LicenseVendor): ApiLicenseResult
}
