package com.simprints.infra.license.remote

import com.simprints.infra.license.LicenseVendor

internal interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String, licenseVendor: LicenseVendor): ApiLicenseResult
}
