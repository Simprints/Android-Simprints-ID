package com.simprints.infralicense.remote

import com.simprints.infralicense.repository.LicenseVendor

interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String, licenseVendor: LicenseVendor): ApiLicenseResult
}
