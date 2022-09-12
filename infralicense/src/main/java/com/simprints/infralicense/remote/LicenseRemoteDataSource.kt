package com.simprints.infralicense.remote

import com.simprints.infralicense.LicenseVendor

internal interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String, licenseVendor: LicenseVendor): ApiLicenseResult
}
